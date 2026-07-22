import os
import re
import random
from collections import defaultdict

quoted_label_re = re.compile(r'label\s*=\s*"([^"]*)"')
html_label_re = re.compile(r'label\s*=\s*<(.+?)>')

action_outputs = defaultdict(set)

# -------------------------
# جمع کردن خروجی‌های ممکن
# -------------------------
for filename in os.listdir('.'):
    if not filename.endswith('.dot'):
        continue

    with open(filename, encoding="utf-8", errors="ignore") as f:
        for line in f:
            if '->' not in line or 'label' not in line:
                continue

            m = quoted_label_re.search(line)
            if m:
                content = m.group(1)
                if '/' in content:
                    a, o = content.split('/', 1)
                    action_outputs[a.strip()].add(o.strip())
                continue

            m = html_label_re.search(line)
            if m:
                content = m.group(1)
                parts = re.split(r'<br\s*/?>', content, maxsplit=1)
                if len(parts) == 2:
                    actions_part, rest = parts
                    for a in actions_part.split('|'):
                        a = a.strip()
                        if a:
                            action_outputs[a].add(rest.strip())

# -------------------------
# انتخاب خروجی رندوم
# -------------------------
chosen_output = {
    action: random.choice(list(outputs))
    for action, outputs in action_outputs.items()
}

print("Chosen outputs:")
for a in sorted(chosen_output):
    print(a, "->", chosen_output[a])

# -------------------------
# بازنویسی فایل‌ها
# -------------------------
for filename in os.listdir('.'):
    if not filename.endswith('.dot'):
        continue

    new_lines = []

    with open(filename, encoding="utf-8", errors="ignore") as f:
        for line in f:

            m = quoted_label_re.search(line)
            if m:
                content = m.group(1)
                if '/' in content:
                    action, _ = content.split('/', 1)
                    action = action.strip()

                    if action in chosen_output:
                        new = f"{action}/{chosen_output[action]}"
                        line = line.replace(content, new)

            m = html_label_re.search(line)
            if m:
                content = m.group(1)
                parts = re.split(r'<br\s*/?>', content, maxsplit=1)

                if len(parts) == 2:
                    actions_part = parts[0]
                    actions = [a.strip() for a in actions_part.split('|') if a.strip()]

                    if actions:
                        action = actions[0]
                        if action in chosen_output:
                            new = f"{actions_part}<br />{chosen_output[action]}"
                            line = line.replace(content, new)

            new_lines.append(line)

    with open(filename, "w", encoding="utf-8") as f:
        f.writelines(new_lines)
