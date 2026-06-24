import os
import re

pattern_quote = re.compile(r'label="([^/"]+)\s*/')
pattern_html = re.compile(r'label=<([^>]+)>')

for filename in os.listdir('.'):
    if not filename.endswith('.dot'):
        continue

    actions = set()

    with open(filename) as f:
        for line in f:
            m = pattern_quote.search(line)
            if m:
                actions.add(m.group(1).strip())
                continue

            m = pattern_html.search(line)
            if m:
                content = m.group(1)
                first_part = content.split("<br")[0]
                for a in first_part.split("|"):
                    actions.add(a.strip())

    print(f"{filename}:")
    for a in sorted(actions):
        print(f"     {a}")
