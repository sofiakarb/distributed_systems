requirevars 'defaultDB' 'input_global_tbl' 'testvalue' 'hypothesis' 'effectsize' 'ci' 'meandiff';
attach database '%{defaultDB}' as defaultDB;

--var 'input_global_tbl' 'defaultDB.localstatistics';

drop table if exists defaultDB.globalstatistics;
create table  defaultDB.globalstatistics as
select colname, S1total/Ntotal as mean, SQROOT( FARITH('/', '-', '*', Ntotal, S2total, '*', S1total, S1total, '*', Ntotal, '-', Ntotal, 1)) as std, Ntotal
from (select colname, sum(S1) as S1total, sum(S2) as S2total, sum(N) as Ntotal
from %{input_global_tbl}
group by colname);

drop table if exists defaultDB.globalttestresult;
create table  defaultDB.globalttestresult as
select * from (ttest_onesample testvalue:%{testvalue} effectsize:%{effectsize} ci:%{ci} meandiff:%{meandiff} hypothesis:%{hypothesis} sediff:0
               select * from   defaultDB.globalstatistics);

var 'resultschema' from select outputschema from defaultDB.globalttestresult limit 1;
var 'typesofresults' from select create_complex_query("","real" , "," , "" , '%{resultschema}');
var 'typesofresults2' from select strreplace(mystring) from (select 'text,real,int,'||'%{typesofresults}' as mystring);


var 'jsonResult' from select '{ "type": "application/json", "data": ' || val ||'}' from
(select tabletojson( colname, statistics, df,%{resultschema}, "colname,statistics,df,%{resultschema}",0) as val
 from defaultDB.globalttestresult);

var 'tableResult' from select * from (totabulardataresourceformat title:INDEPENDENT_TEST_TABLE types:%{typesofresults2}
              select colname,statistics,df,%{resultschema} from defaultDB.globalttestresult);

select '{"result": [' || '%{jsonResult}' || ',' || '%{tableResult}' || ']}';

--
-- drop table if exists defaultDB.ttestresultvisual;
-- create table defaultDB.ttestresultvisual as
-- setschema 'result'
-- select * from (totabulardataresourceformat title:ONE_SAMPLE_T_TEST_TABLE types:%{typesofresults2}
--                select colname,statistics,df,%{resultschema} from defaultDB.globalttestresult);
--
-- select * from defaultDB.ttestresultvisual;
