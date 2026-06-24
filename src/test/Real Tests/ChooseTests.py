import os
import random
import shutil
from typing import Final
import copy

COMPONENTS: Final[list] = [
                "10_learnresult_MasterCard_fix.dot",
                "1_learnresult_MasterCard_fix.dot",
                "4_learnresult_MAESTRO_fix.dot",
                "4_learnresult_PIN_fix.dot",
                "4_learnresult_SecureCode Aut_fix.dot",
                "ASN_learnresult_MAESTRO_fix.dot",
                "ASN_learnresult_SecureCode Aut_fix.dot",
                "learnresult_fix.dot",
                "Rabo_learnresult_MAESTRO_fix.dot",
                "Rabo_learnresult_SecureCode_Aut_fix.dot",
                "Volksbank_learnresult_MAESTRO_fix.dot"
             ]

TESTS_FOLDER = "src/test/Real Tests/resources/"

for filename in os.listdir("src/test/Real Tests/data"):
    
    file_path = os.path.join("src/test/Real Tests/data", filename)
    try:
        if os.path.isfile(file_path) or os.path.islink(file_path):
            os.unlink(file_path)
        elif os.path.isdir(file_path):
            shutil.rmtree(file_path)
    except Exception as e:
        print('Failed to delete %s. Reason: %s' % (file_path, e))
f = open("Configs/Real Tests.txt", "r")
numOfComponents = int (f.readline())
numOfTests = int(f.readline())
for outerRepeat in range(0, numOfTests):
    remaindedComponents = numOfComponents
    curComponents = copy.copy(COMPONENTS)
    chosen = ''
    while(remaindedComponents > 0):
        componentIndex = random.randint(0, len(curComponents) - 1)
        chosen += TESTS_FOLDER + curComponents[componentIndex] + '\n'
        curComponents.pop(componentIndex)
        remaindedComponents -= 1

    outerInput = 'src/test/Real Tests/data/Reals.txt'
    innerInput = 'src/test/Real Tests/data/Reals_With_' + str(numOfComponents) + '_Components' + str(outerRepeat + 1) + '.txt'
    with open(outerInput, 'a') as writingfile:
        writingfile.write(innerInput + '\n')
        writingfile.close()
    with open( innerInput, 'w') as writingfile:
        writingfile.write(chosen)
        writingfile.close()