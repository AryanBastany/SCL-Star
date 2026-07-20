import os
import random
import shutil
from typing import Final
import copy

COMPONENTS: Final[list] = [
                 "4_1_BCS_MPW.dot", "4_2_BCS_APW.dot",
                 "4_3_BCS_FP.dot", "4_5_BCS_LED_FP.dot",
                 "4_6_BCS_CLS.dot", "4_7_BCS_EM.dot",
                 "4_9_BCS_LED_CLS.dot", "4_10_BCS_LED_MPW.dot",
                 "4_11_BCS_LED_APW.dot", "4_12_BCS_LED_EMT.dot",
                 "4_13_MCS_LED_EML.dot", "4_14_BCS_LED_EMB.dot",
                 "4_15_BCS_LED_EMR.dot", "4_16_BCS_LED_EMH.dot",
                 "4_17_BCS_LED_AS_Active.dot", "4_18_BCS_LED_AS_Alarm.dot",
                 "4_19_BCS_LED_AS_Alarm_D.dot", "4_20_MCS_LED_AS_IMA.dot",
                 "4_21_BCS_AS.dot", "BCS_PW_4.dot"
             ]
SYNCHS: Final[list] = [
            [
                "4_1_BCS_MPW.dot", "4_2_BCS_APW.dot",
                "4_3_BCS_FP.dot", "BCS_PW_4.dot"
            ]
            ,
            [
                "4_6_BCS_CLS.dot", "4_21_BCS_AS.dot"
            ]
            ,
            [
                "4_2_BCS_APW.dot", "4_5_BCS_LED_FP.dot"
            ]
            ,
            [
                "4_1_BCS_MPW.dot", "4_5_BCS_LED_FP.dot"
            ]
            ,
            [
                "4_12_BCS_LED_EMT.dot", "4_14_BCS_LED_EMB.dot"
            ]
            ,
            [
                "4_13_MCS_LED_EML.dot", "4_15_BCS_LED_EMR.dot"
            ]
            ,
            [
                "4_5_BCS_LED_FP.dot", "BCS_PW_4.dot"
            ]
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
    numOfSynchs = random.randint(1, min(int(numOfComponents/2), 4))
    curSynchs = copy.copy(SYNCHS)
    curComponents = copy.copy(COMPONENTS)
    chosen = ''
    biggestSynch = 4
    while(numOfSynchs):
        if((2 * numOfSynchs) < biggestSynch):
            curSynchs.pop(0)
            biggestSynch = 0
        currentSynchIndex = random.randint(0, len(curSynchs) - 1)
        currentSynch = curSynchs[currentSynchIndex]
        for component in currentSynch:
            if(component in curComponents):
                curComponents.remove(component)
                chosen += TESTS_FOLDER + "Synchs/" + component + '\n'
                remaindedComponents-= 1

                if biggestSynch != 0 and component in curSynchs[0]:
                    biggestSynch -= 1

        curSynchs.remove(currentSynch)

        numOfSynchs -= 1
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