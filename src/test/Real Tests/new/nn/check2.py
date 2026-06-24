import os
import re
from collections import defaultdict

# label="action/output"
quoted_label_re = re.compile(r'label\s*=\s*"([^"]*)"')

# label=<...>
html_label_re = re.compile(r'label\s*=\s*<(.+?)>', re.DOTALL)

global_action_outputs = defaultdict(set)

for filename in os.listdir('.'):
    if not filename.endswith('.dot'):
        continue

    with open(filename, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            line = line.strip()

            # فقط transitionها مهم هستند
            if '->' not in line or 'label' not in line:
                continue

            # حالت 1: label="..."
            m = quoted_label_re.search(line)
            if m:
                content = m.group(1).strip()

                if '/' in content:
                    action, output = content.split('/', 1)
                    action = action.strip()
                    output = output.strip()
                    if action:
                        global_action_outputs[action].add(output)

                continue

            # حالت 2: label=<...>
            m = html_label_re.search(line)
            if m:
                content = m.group(1).strip()

                parts = re.split(r'<br\s*/?>', content, maxsplit=1)
                if len(parts) == 2:
                    actions_part, output = parts
                    output = output.strip()

                    actions = [a.strip() for a in actions_part.split('|') if a.strip()]
                    for action in actions:
                        global_action_outputs[action].add(output)

# چاپ خروجی نهایی
for action in sorted(global_action_outputs):
    print(f"{action}:")
    for output in sorted(global_action_outputs[action]):
        print(f"     {output}")
