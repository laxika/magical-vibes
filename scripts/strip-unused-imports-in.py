#!/usr/bin/env python3
"""strip-unused-imports.py for the given files only. Usage: python3 scripts/strip-unused-imports-in.py FILE..."""
import importlib.util
import os
import sys
from pathlib import Path

spec = importlib.util.spec_from_file_location(
    "strip", os.path.join(os.path.dirname(__file__), "strip-unused-imports.py"))
strip = importlib.util.module_from_spec(spec)
spec.loader.exec_module(strip)
for name in sys.argv[1:]:
    removed = strip.process(Path(name), True)
    if removed:
        print(name, removed)
