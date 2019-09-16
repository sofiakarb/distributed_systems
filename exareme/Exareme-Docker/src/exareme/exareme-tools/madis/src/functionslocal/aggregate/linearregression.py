# import functions
# import itertools
# import math
# from array import *
#
# try:
#     from collections import OrderedDict
# except ImportError:
#     # Python 2.6
#     from lib.collections26 import OrderedDict
#
# __docformat__ = 'reStructuredText en'
#
#
# class modelformulaeold:
#     # '+','*',':'
#
#     registered = True  # Value to define db operator
#
#     def __init__(self):
#         self.init = True
#         self.datagroup = dict()
#         self.colnames = []
#         self.formula = None
#         self.rid = None
#         self.all = True
#
#     def step(self, *args):
#         # print args
#         if self.init:
#             self.init = False
#             if args[0] == 'all':
#                 self.all = True
#                 self.formula = args[4]
#                 self.rid = args[1]
#             else:
#                 self.all = False
#                 self.formula = args[3]
#                 self.rid = args[0]
#             if not args:
#                 raise functions.OperatorError("modelFormulae", "No arguments")
#             # print args
#         if self.all == True:
#             self.datagroup[args[2]] = args[3]
#         else:
#             self.datagroup[args[1]] = args[2]
#
#     def final(self):
#         yield ('rid', 'colname', 'val')
#         if self.formula != None:
#             for formulaPart in self.formula.split('+'):
#                 if ('*' in formulaPart) or (':' in formulaPart):
#                     fpartsA = None
#                     if ('*' in formulaPart):
#                         fpartsA = formulaPart.split('*')
#                     elif (':' in formulaPart):
#                         fpartsA = formulaPart.split(':')
#
#                     fpartsCorrectColumns = [[] for x in range(len(fpartsA))]
#                     for f in xrange(0, len(fpartsA)):
#                         for data in self.datagroup:
#                             if fpartsA[f] in data:
#                                 fpartsCorrectColumns[f].append(data)
#
#                     fpartsB = list(itertools.product(*fpartsCorrectColumns))  # to dp
#                     fpartsB.sort()
#                     if self.all == False and len(fpartsB) > 1:
#                         fpartsB.pop(0)
#
#                     fpartsAll = []
#                     for fp in fpartsB:
#                         fparts = [fp[0], fp[1]]
#
#                         binaryNumberLength = '{0:0' + str(len(fparts)) + 'b}'
#                         for binaryNumber in xrange(1, int(math.pow(2, len(fparts)))):  # create binary combinations
#                             colname = ''
#                             colval = None
#                             no = 0
#                             for p in xrange(0, len(fparts)):  # for each
#                                 if binaryNumberLength.format(binaryNumber)[p] == '1':
#                                     no = no + 1
#                                     colname = colname + ":" + fparts[p]
#                                     if colval == None:
#                                         colval = self.datagroup[fparts[p]]
#                                     else:
#                                         colval = colval * self.datagroup[fparts[p]]
#                             if '*' in formulaPart and colname[1:] not in fpartsAll:
#                                 yield self.rid, colname[1:], colval
#                                 fpartsAll.append(colname[1:])
#                             elif ':' in formulaPart and no > 1 and colname[1:] not in fpartsAll:
#                                 yield self.rid, colname[1:], colval
#                                 fpartsAll.append(colname[1:])
#                 else:
#                     col = []
#                     for data in sorted(self.datagroup):
#                         if formulaPart in data:
#                             col.append(data)
#                     if self.all == False and len(col) > 1:
#                         col.pop(0)
#                     for data in col:
#                         yield self.rid, data, self.datagroup[data]
#                     # fpartsAll.append(data)
#
#
# if not ('.' in __name__):
#     """
#     This is needed to be able to test the function, put it at the end of every
#     new function you create
#     """
#     import sys
#     from functions import *
#
#     testfunction()
#     if __name__ == "__main__":
#         reload(sys)
#         sys.setdefaultencoding('utf-8')
#
#
