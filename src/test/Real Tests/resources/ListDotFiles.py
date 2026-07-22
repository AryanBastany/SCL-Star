import os
import re

def extract_number(filename):
    """
    عدد داخل نام فایل را استخراج می‌کند.
    مثلا hyp.120.obf.dot -> 120
    اگر عددی نبود، مقدار خیلی بزرگ برمی‌گرداند.
    """
    m = re.search(r'\.(\d+)\.', filename)
    return int(m.group(1)) if m else float('inf')

# گرفتن فایل‌های .dot
dot_files = [f for f in os.listdir('.') if f.endswith('.dot')]

# مرتب‌سازی عددی
dot_files.sort(key=extract_number)

# چاپ با فرمت دلخواه
for f in dot_files:
    print(f'                 "{f}",')
