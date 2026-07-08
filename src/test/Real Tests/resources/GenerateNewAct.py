import os
import re
import random
import string

dot_files = sorted([f for f in os.listdir('.') if f.endswith('.dot')])

quoted_label_re = re.compile(r'label\s*=\s*"([^"]*)"')
html_label_re = re.compile(r'label\s*=\s*<(.+?)>')
edge_re = re.compile(r'^(\s*)(s\d+)\s*->')
state_re = re.compile(r'\b(s\d+)\b')

existing_actions = set()

# -------------------------
# استخراج actionهای موجود
# -------------------------
for file in dot_files:
    with open(file, encoding="utf-8", errors="ignore") as f:
        content = f.read()

    for m in quoted_label_re.findall(content):
        if "/" in m:
            existing_actions.add(m.split("/")[0].strip())

    for m in html_label_re.findall(content):
        parts = re.split(r'<br\s*/?>', m)
        for p in parts:
            if "/" in p:
                existing_actions.add(p.split("/")[0].strip())

# -------------------------
# تولید action جدید
# -------------------------
def random_action(length=8):
    return ''.join(random.choices(string.ascii_letters, k=length))

new_actions = []
while len(new_actions) < len(dot_files):
    a = random_action()
    if a not in existing_actions and a not in new_actions:
        new_actions.append(a)

# -------------------------
# تولید output (فقط 0 یا 1)
# -------------------------
def random_output():
    return random.choice(["0", "1"])

# -------------------------
# پردازش فایل‌ها
# -------------------------
for idx, file in enumerate(dot_files):

    action = new_actions[idx]

    with open(file, encoding="utf-8", errors="ignore") as f:
        lines = f.readlines()

    content = "".join(lines)
    states = sorted(set(state_re.findall(content)), key=lambda x: int(x[1:]))

    last_edge_index = {}
    last_indent = {}

    for i, line in enumerate(lines):
        m = edge_re.match(line)
        if m:
            indent = m.group(1)
            state = m.group(2)
            last_edge_index[state] = i
            last_indent[state] = indent

    for state in reversed(states):

        if state not in last_edge_index:
            continue

        target = random.choice(states)
        insert_pos = last_edge_index[state] + 1
        indent = last_indent[state]
        
        # تولید خروجی مستقل برای هر ایالت
        output = random_output()

        new_line = f'{indent}{state} -> {target}[label="{action}/{output}"]\n'
        lines.insert(insert_pos, new_line)

    with open(file, "w", encoding="utf-8") as f:
        f.writelines(lines)

print("Generated actions:")
for f, a in zip(dot_files, new_actions):
    print(f"{f}: {a} (outputs dynamically set to '0' or '1' per state)")
