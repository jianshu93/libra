#! /usr/bin/env python

import os
import os.path
import sys
import json

def getFileTable(path):
    tbl = None
    with open(path, 'r') as f:
        for line in f:
            j = json.loads(line)
            tbl = j["sample_files"]

    return tbl

def getScore(path):
    max_f1 = 0
    max_f2 = 0
    with open(path, 'r') as f:
        for line in f:
            vv = line.split()
            f1 = int(vv[0]) + 1
            f2 = int(vv[1]) + 1

            if max_f1 < f1:
                max_f1 = f1

            if max_f2 < f2:
                max_f2 = f2

    max_f = 0
    if max_f1 > max_f2:
        max_f = max_f1
    else:
        max_f = max_f2

    matrix = [[0 for x in range(0, max_f)] for y in range(0, max_f)]
    with open(path, 'r') as f:
        for line in f:
            vv = line.split()
            f1 = int(vv[0].strip())
            f2 = int(vv[1].strip())
            score = float(vv[2].strip())
            if score > 1:
                score = 1.0
            matrix[f1][f2] = score

    return matrix

def writeMatrix(out_matrix, file_table, score, dissimilarity=False):
    with open(out_matrix, "w") as f:
        f.write("\t")
        for i in range(0, len(score)):
            if i != 0:
                f.write("\t")
            f.write(os.path.basename(file_table[i]))
        f.write("\n")

        for i in range(0, len(score)):
            row = score[i]
            f.write(os.path.basename(file_table[i]))
            for j in range(0, len(row)):
                f.write("\t")
                if dissimilarity:
                    f.write(str(1 - row[j]))
                else:
                    f.write(str(row[j]))
            f.write("\n")

def main(argv):
    if len(argv) < 3:
        print "command : ./gen_score_matrix.py result_score table_json out_matrix [distance|similiarity]"
    else:
        result_score = argv[0]
        table_json = argv[1]
        out_matrix = argv[2]
        dissimilarity = False
        if len(argv) == 4:
            if argv[3] in ["distance", "dissimilarity"]:
                dissimilarity = True
        farr = getFileTable(table_json)
        score = getScore(result_score)
        writeMatrix(out_matrix, farr, score, dissimilarity)

if __name__ == "__main__":
    main(sys.argv[1:])
