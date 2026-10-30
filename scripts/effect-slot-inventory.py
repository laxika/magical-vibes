#!/usr/bin/env python3
"""Builds effect-slot-inventory.md: one row per EffectSlot constant (step 1 of effect-slot-refactor.md).

The mechanical columns are read from the sources on every run:

- the card classes (magical-vibes-card) that use each constant, the effect classes and trigger
  modes they register in it, and the wrapper, ...TriggerEffect and ...AwareEffect classes involved;
- every method in the engine, domain, AI and networking main sources that names the constant
  (EffectSlot.X, a @CollectsTrigger(slot = ...) handler, a case label of a switch on a slot, or a
  slot.name() prefix test), with a guess at how it groups several registrations of one card.

The semantic columns (kind, event, filters, zone, multiplicity, self-inclusion, refactor step and
reviewed grouping) come from scripts/effect-slot-classification.tsv, which is maintained by hand.

The baseline section maps every refactor family to the card test classes of its cards and, when
--results points at JUnit XML reports (build/test-results/test of the card-test modules), to the
classes among them that failed.

Usage:
  python3 scripts/effect-slot-inventory.py                 # rewrite effect-slot-inventory.md
  python3 scripts/effect-slot-inventory.py --json out.json # also dump the raw data
  python3 scripts/effect-slot-inventory.py --results       # read the card-test JUnit reports
  python3 scripts/effect-slot-inventory.py --all-passed    # record a full run with no failures
  python3 scripts/effect-slot-inventory.py --check         # fail when the TSV misses a constant
  python3 scripts/effect-slot-inventory.py --gradle-args 11 # Gradle filters for a step's card tests
"""

import argparse
import collections
import csv
import datetime
import glob
import json
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EFFECT_SLOT = ROOT / 'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/EffectSlot.java'
EFFECT_DIR = ROOT / 'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect'
CLASSIFICATION = ROOT / 'scripts/effect-slot-classification.tsv'
OUTPUT = ROOT / 'effect-slot-inventory.md'

READER_ROOTS = [
    ('engine', ROOT / 'magical-vibes-engine/src/main/java'),
    ('domain', ROOT / 'magical-vibes-domain/src/main/java'),
    ('ai', ROOT / 'magical-vibes-ai/src/main/java'),
    ('networking', ROOT / 'magical-vibes-networking/src/main/java'),
]
FIXTURE_ROOTS = [
    ('engine-fixtures', ROOT / 'magical-vibes-engine/src/testFixtures/java'),
]

KINDS = ['spell', 'static', 'activated', 'mana', 'replacement', 'special action', 'marker', 'triggered']

FAMILIES = collections.OrderedDict([
    ('5', 'Step 5: spell abilities'),
    ('6', 'Step 6: static abilities'),
    ('7', 'Step 7: activated and mana abilities'),
    ('8', 'Step 8: replacement effects, special actions and markers'),
    ('10', 'Step 10: pilot (the source\'s own zone changes)'),
    ('11', 'Step 11: creature-death triggers'),
    ('12', 'Step 12: the rest of the zone-change family'),
    ('13.1', 'Step 13.1: steps and phases'),
    ('13.2', 'Step 13.2: spells and abilities'),
    ('13.3', 'Step 13.3: combat'),
    ('13.4', 'Step 13.4: damage'),
    ('13.5', 'Step 13.5: counters'),
    ('13.6', 'Step 13.6: becomes the target'),
    ('13.7', 'Step 13.7: library, draw and discard'),
    ('13.8', 'Step 13.8: tap and untap'),
    ('13.9', 'Step 13.9: life'),
    ('13.10', 'Step 13.10: sagas'),
    ('13.11', 'Step 13.11: everything else'),
])

TSV_COLUMNS = ['constant', 'kind', 'family', 'event', 'object', 'relation', 'zone', 'multiplicity',
               'self', 'grouping', 'notes']


def strip_java(src):
    """Blanks comments and the contents of string, text-block and char literals, keeping offsets."""
    out = list(src)
    i, n = 0, len(src)

    def blank(a, b):
        for k in range(a, b):
            if out[k] != '\n':
                out[k] = ' '

    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            j = src.find('\n', i)
            j = n if j == -1 else j
            blank(i, j)
            i = j
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            j = src.find('*/', i + 2)
            j = n if j == -1 else j + 2
            blank(i, j)
            i = j
        elif src.startswith('"""', i):
            j = src.find('"""', i + 3)
            j = n if j == -1 else j + 3
            blank(i + 3, j - 3)
            i = j
        elif c == '"' or c == "'":
            j = i + 1
            while j < n and src[j] != c:
                j += 2 if src[j] == '\\' else 1
            blank(i + 1, j)
            i = j + 1
        else:
            i += 1
    return ''.join(out)


TYPE_RE = re.compile(r'\b(class|interface|enum|record)\s+(\w+)')
CONTROL_WORDS = {'if', 'for', 'while', 'switch', 'catch', 'synchronized', 'try', 'else', 'do', 'return',
                 'new', 'throw'}


def _last_paren_group(header):
    """Returns (name, start) of the identifier before the header's last top-level (...) group."""
    h = re.sub(r'\bthrows\s+[\w.,\s<>]+$', '', header.rstrip()).rstrip()
    if not h.endswith(')'):
        return None
    depth = 0
    for k in range(len(h) - 1, -1, -1):
        if h[k] == ')':
            depth += 1
        elif h[k] == '(':
            depth -= 1
            if depth == 0:
                m = re.search(r'(\w+)\s*$', h[:k])
                return m.group(1) if m else None
    return None


def find_spans(code):
    """Finds type and method spans: dicts with kind, name, hstart, open, end, parent index."""
    spans = []
    stack = []
    boundary = 0
    for i, c in enumerate(code):
        if c == '{':
            j = i - 1
            while j >= 0 and code[j].isspace():
                j -= 1
            prev = code[j] if j >= 0 else ''
            if prev in '(,=]' or code[max(0, j - 1):j + 1] == '{{':
                stack.append({'kind': 'array', 'name': None, 'hstart': boundary, 'open': i})
                continue
            header = code[boundary:i]
            parent = next((s for s in reversed(stack) if s['kind'] != 'array'), None)
            kind, name = 'block', None
            in_type = parent is None or parent['kind'] == 'type'
            tm = TYPE_RE.search(header)
            if tm and (in_type or '=' not in header):
                kind, name = 'type', tm.group(2)
            elif in_type:
                name = _last_paren_group(header)
                if name and name not in CONTROL_WORDS:
                    kind = 'method'
                elif parent is not None and re.fullmatch(r'\s*(?:@\w+\s*)*(?:public|protected|private)?\s*(\w+)\s*',
                                                         header) and header.strip().split()[-1] == parent['name']:
                    kind, name = 'method', '<init>'
                elif re.fullmatch(r'\s*static\s*', header):
                    kind, name = 'method', '<clinit>'
            elif re.search(r'\bnew\s+[\w.<>]+\s*\([^;]*\)\s*$', header):
                kind, name = 'type', '<anonymous>'
            stack.append({'kind': kind, 'name': name, 'hstart': boundary, 'open': i})
            boundary = i + 1
        elif c == '}':
            if not stack:
                continue
            top = stack.pop()
            if top['kind'] == 'array':
                continue
            top['end'] = i
            top['parents'] = [s['name'] for s in stack if s['kind'] in ('type', 'method')]
            if top['kind'] in ('type', 'method'):
                spans.append(top)
            boundary = i + 1
        elif c == ';':
            if stack and stack[-1]['kind'] == 'array':
                continue
            boundary = i + 1
    return spans


def enclosing(spans, pos):
    """The innermost method span holding pos (or None) and the innermost type span."""
    method, typ = None, None
    for s in spans:
        if s['hstart'] <= pos <= s['end']:
            if s['kind'] == 'method' and (method is None or s['hstart'] >= method['hstart']):
                method = s
            if s['kind'] == 'type' and (typ is None or s['open'] >= typ['open']):
                typ = s
    return method, typ


def balanced_args(code, open_paren):
    """Splits the argument list starting at code[open_paren] == '(' into top-level arguments."""
    depth = 0
    args, start = [], open_paren + 1
    for k in range(open_paren, len(code)):
        ch = code[k]
        if ch in '([{':
            depth += 1
        elif ch in ')]}':
            depth -= 1
            if depth == 0:
                args.append(code[start:k])
                return args, k
        elif ch == ',' and depth == 1:
            args.append(code[start:k])
            start = k + 1
    return args, len(code)


def parse_slots():
    src = EFFECT_SLOT.read_text()
    body = src[src.index('{') + 1:src.rindex('}')]
    constants = []
    doc = None
    pos = 0
    token = re.compile(r'/\*\*(.*?)\*/|//[^\n]*|([A-Z][A-Z0-9_]*)\s*[,;]?', re.S)
    while pos < len(body):
        m = token.search(body, pos)
        if not m:
            break
        if m.group(1) is not None:
            doc = ' '.join(line.strip().lstrip('*').strip() for line in m.group(1).splitlines()).strip()
        elif m.group(2):
            constants.append((m.group(2), doc or ''))
            doc = None
        pos = m.end()
    return constants


def java_files(root):
    for dirpath, _, files in os.walk(root):
        if '/build/' in dirpath + '/':
            continue
        for f in files:
            if f.endswith('.java'):
                yield Path(dirpath) / f


TRIGGER_WRAPPER = re.compile(r'^May\w*Effect$|Conditional|^Once\w*Effect$|Exclusion|^SequenceEffect$|^ChooseOne')


def effect_class_info():
    """Trigger-level wrappers, ...TriggerEffect classes and the ...AwareEffect interfaces effects implement.

    A trigger-level wrapper is a record with a CardEffect component whose name marks it as a choice,
    condition, limit or grouping around the wrapped effect (MayEffect, ConditionalEffect,
    OncePerTurnTriggerEffect, TriggeringPermanentConditionalEffect, SequenceEffect, ...).
    """
    wrappers, trigger_effects, aware = set(), set(), collections.defaultdict(set)
    aware_names = set()
    for path in java_files(EFFECT_DIR):
        name = path.stem
        if name.endswith('AwareEffect'):
            aware_names.add(name)
    for path in java_files(EFFECT_DIR):
        name = path.stem
        code = strip_java(path.read_text())
        decl = re.search(r'\b(record|class|interface)\s+' + re.escape(name) + r'\b', code)
        if not decl:
            continue
        if name.endswith('TriggerEffect'):
            trigger_effects.add(name)
        if decl.group(1) == 'record':
            args, close = balanced_args(code, code.index('(', decl.end()))
            components = ' '.join(args)
            if re.search(r'(?<![\w.])(CardEffect|List<\s*(?:\?\s*extends\s+)?CardEffect\s*>)\s+\w+', components) \
                    and TRIGGER_WRAPPER.search(name) and not name.endswith('TriggerEffect'):
                wrappers.add(name)
            header = code[close:code.index('{', close)]
        else:
            header = code[decl.end():code.index('{', decl.end())]
        for a in aware_names:
            if re.search(r'\b' + a + r'\b', header):
                aware[name].add(a)
    return wrappers, trigger_effects, aware, aware_names


REGISTER_CALLS = ('addEffect', 'addSimultaneousEffects')


def scan_cards(slot_names, wrappers, trigger_effects, aware):
    """Per constant: card files using it and the registrations made through addEffect calls."""
    per_slot = {s: {'cards': set(), 'regs': [], 'multi': set(), 'modes': collections.Counter(),
                    'wrappers': collections.Counter(), 'triggers': collections.Counter(),
                    'aware': collections.Counter(), 'outer': collections.Counter(),
                    'unresolved': 0, 'simultaneous': 0} for s in slot_names}
    slot_ref = re.compile(r'EffectSlot\.([A-Z][A-Z0-9_]*)')
    card_roots = sorted((ROOT / 'magical-vibes-card').glob('*/src/main/java'))
    for root in card_roots:
        for path in java_files(root):
            raw = path.read_text()
            if 'EffectSlot' not in raw:
                continue
            code = strip_java(raw)
            card = path.stem
            regs_in_file = collections.Counter()
            for m in slot_ref.finditer(code):
                s = m.group(1)
                if s not in per_slot:
                    continue
                per_slot[s]['cards'].add(card)
                call = re.search(r'(\w+)\s*\(\s*$', code[max(0, m.start() - 80):m.start()])
                if not call or call.group(1) not in REGISTER_CALLS:
                    continue
                open_paren = code.rindex('(', 0, m.start())
                args, _ = balanced_args(code, open_paren)
                effect_args = [a.strip() for a in args[1:]]
                mode = next((a.split('.')[-1] for a in effect_args if a.startswith('TriggerMode.')), 'NORMAL')
                effect_args = [a for a in effect_args if not a.startswith('TriggerMode.')]
                if call.group(1) == 'addSimultaneousEffects':
                    per_slot[s]['simultaneous'] += 1
                per_slot[s]['modes'][mode] += 1
                for expr in effect_args:
                    regs_in_file[s] += 1
                    resolved = expr
                    if re.fullmatch(r'\w+', expr):
                        assign = re.search(r'\b' + re.escape(expr) + r'\s*=\s*(new\s+\w+\s*\()', code)
                        if assign:
                            a_args, a_end = balanced_args(code, code.index('(', assign.start(1)))
                            resolved = code[assign.start(1):a_end + 1]
                    found = [(m2.start(), m2.group(1)) for m2 in re.finditer(r'\bnew\s+(\w+)\s*\(', resolved)] + \
                        [(m2.start(), m2.group(1)) for m2 in re.finditer(r'\b([A-Z]\w*Effect)\s*\.\s*\w+\s*\(', resolved)]
                    classes = [cls for _, cls in sorted(found)]
                    effects = [cls for cls in classes if cls.endswith('Effect')]
                    if not effects:
                        per_slot[s]['unresolved'] += 1
                        continue
                    per_slot[s]['outer'][effects[0]] += 1
                    for cls in set(classes):
                        if cls in wrappers:
                            per_slot[s]['wrappers'][cls] += 1
                        if cls in trigger_effects:
                            per_slot[s]['triggers'][cls] += 1
                        for a in aware.get(cls, ()):
                            per_slot[s]['aware'][a] += 1
            for s, n in regs_in_file.items():
                per_slot[s]['regs'].append(n)
                if n > 1:
                    per_slot[s]['multi'].add(card)
    return per_slot


def method_label(path, spans, method, typ):
    cls = path.stem
    if method is None:
        return f'{cls} (field)' if typ is not None else cls
    inner = [p for p in method['parents'] if p and p != cls and p != '<anonymous>']
    owner = '.'.join([cls] + [p for p in inner if p[0].isupper()])
    return f'{owner}#{method["name"]}'


def grouping_guess(body, dispatchers):
    """How a method seems to turn one card's registrations in a slot into stack objects."""
    if '@CollectsTrigger' in body:
        # The registry hands a handler one registration at a time, so it can only queue that one,
        # unless it reads the slot again.
        return 'all' if re.search(r'getEffects\(|getEffectRegistrations\(', body) else 'each'
    if 'TriggerMode.INDEPENDENT' in body:
        return 'mode'
    if 'registry.dispatch(' in body or any(re.search(r'\b' + d + r'\s*\(', body) for d in dispatchers):
        return 'each'
    queues = re.search(r'new StackEntry\(|enqueueTrigger\(|queueMayAbility\(|queueInteraction\(|stack\.add\(', body)
    if not queues:
        return 'read'
    all_pat = re.search(r'new ArrayList<>\(\s*(effects|\w*[eE]ffects|\w+\.getEffects\([^)]*\))\s*\)'
                        r'|List\.copyOf\(\s*\w*[eE]ffects\s*\)', body)
    each_pat = re.search(r'List\.of\(\s*(effect|e|eff|wrapped|inner|resolved|bound|\w*Effect)\s*\)', body)
    if all_pat and each_pat:
        return 'mixed'
    if all_pat:
        return 'all'
    if each_pat:
        return 'each'
    return 'queue?'


def scan_readers(slot_names, aware_names):
    slot_set = set(slot_names)
    refs = collections.defaultdict(list)
    generic = []
    dispatchers = set()
    sources = []
    for module, root in READER_ROOTS + FIXTURE_ROOTS:
        for path in java_files(root):
            if path == EFFECT_SLOT:
                continue
            raw = path.read_text()
            if 'EffectSlot' not in raw and 'registry.dispatch' not in raw:
                continue
            sources.append((module, path, raw))
    helpers = []
    for module, path, raw in sources:
        code = strip_java(raw)
        for s in find_spans(code):
            if s['kind'] != 'method':
                continue
            body = code[s['open']:s['end']]
            if 'registry.dispatch(' in body and s['name'].startswith('dispatch'):
                dispatchers.add(s['name'])
            elif s['name'].startswith('dispatch'):
                helpers.append((s['name'], body))
    for _ in range(3):
        for name, body in helpers:
            if any(re.search(r'\b' + d + r'\s*\(', body) for d in dispatchers):
                dispatchers.add(name)
    for module, path, raw in sources:
        if 'EffectSlot' not in raw:
            continue
        code = strip_java(raw)
        spans = find_spans(code)
        named = collections.defaultdict(set)
        for s in spans:
            if s['kind'] == 'method':
                named[s['name']] |= set(re.findall(r'\b(\w+AwareEffect)\b', code[s['open']:s['end']]))
        aware_cache = {}

        def aware_in(method):
            """Aware interfaces a method names, directly or through a helper of the same class it calls."""
            key = method['open']
            if key not in aware_cache:
                body = code[method['open']:method['end']]
                found = set(re.findall(r'\b(\w+AwareEffect)\b', body))
                for callee in set(re.findall(r'\b(\w+)\s*\(', body)):
                    found |= named.get(callee, set())
                aware_cache[key] = found & aware_names
            return aware_cache[key]

        hits = []
        for m in re.finditer(r'\bEffectSlot\.([A-Z][A-Z0-9_]*)\b', code):
            if m.group(1) in slot_set:
                hits.append((m.start(), m.group(1), 'ref'))
        for sw in re.finditer(r'\bswitch\s*\(\s*slot\s*\)\s*\{', code):
            _, close = balanced_args(code, sw.end() - 1)
            block = code[sw.end():close]
            for case in re.finditer(r'\bcase\s+([A-Z0-9_,\s]+?)\s*(->|:)', block):
                for name in re.split(r'[,\s]+', case.group(1).strip()):
                    if name in slot_set:
                        hits.append((sw.end() + case.start(), name, 'switch'))
        for nm in re.finditer(r'\.name\(\)\s*\.\s*(startsWith|endsWith|contains)\(\s*"([A-Z_]+)"', raw):
            for name in slot_names:
                test = {'startsWith': name.startswith, 'endsWith': name.endswith,
                        'contains': name.__contains__}[nm.group(1)]
                if test(nm.group(2)):
                    hits.append((nm.start(), name, 'name ' + nm.group(1) + ' "' + nm.group(2) + '"'))
        if re.search(r'\bEffectSlot\.values\(\)', code):
            for m in re.finditer(r'\bEffectSlot\.values\(\)', code):
                method, typ = enclosing(spans, m.start())
                generic.append((module, method_label(path, spans, method, typ)))
        for pos, name, how in hits:
            method, typ = enclosing(spans, pos)
            label = method_label(path, spans, method, typ)
            header = code[method['hstart']:method['open']] if method else ''
            handled = None
            aware = set()
            if method is not None:
                body = code[method['hstart']:method['end']]
                if '@CollectsTrigger' in header and pos < method['open']:
                    how = 'handler'
                    annotation = code[code.rfind('@CollectsTrigger(', 0, pos):pos]
                    m = re.search(r'(?:value\s*=\s*|\(\s*)(\w+)\.class', annotation)
                    handled = m.group(1) if m else None
                group = grouping_guess(body if how == 'handler' else code[method['open']:method['end']],
                                       dispatchers)
                aware = aware_in(method)
            else:
                group = 'read'
            refs[name].append({'module': module, 'method': label, 'name': method['name'] if method else None,
                               'how': how, 'group': group, 'handles': handled, 'aware': sorted(aware),
                               'file': str(path.relative_to(ROOT)),
                               'line': code.count('\n', 0, pos) + 1})
    return refs, sorted(set(generic)), sorted(dispatchers)


def scan_callers(method_names):
    """Call sites (Class#method) in engine main code of each named method, outside its own body."""
    callers = collections.defaultdict(set)
    if not method_names:
        return callers
    pattern = re.compile(r'\b(' + '|'.join(sorted(map(re.escape, method_names))) + r')\s*\(')
    for path in java_files(ROOT / 'magical-vibes-engine/src/main/java'):
        raw = path.read_text()
        if not pattern.search(raw):
            continue
        code = strip_java(raw)
        spans = None
        for m in pattern.finditer(code):
            if spans is None:
                spans = find_spans(code)
            method, typ = enclosing(spans, m.start())
            if method is None or method['name'] == m.group(1) or m.start() < method['open']:
                continue
            callers[m.group(1)].add(method_label(path, spans, method, typ))
    return callers


def domain_slot_types():
    """Domain types other than Card and EffectSlot that hold or take an EffectSlot."""
    types = []
    root = ROOT / 'magical-vibes-domain/src/main/java'
    for path in java_files(root):
        if path.stem in ('EffectSlot', 'Card'):
            continue
        code = strip_java(path.read_text())
        if not re.search(r'\bEffectSlot\b(?!\s*\.)', re.sub(r'^\s*import\s+[^;]+;', '', code, flags=re.M)):
            continue
        kinds = []
        if re.search(r'\b(Map|EnumMap|Set|EnumSet|List)\s*<\s*EffectSlot\b', code):
            kinds.append('slot-keyed collection')
        if re.search(r'\bEffectSlot\s+\w+\s*[,)]', code):
            kinds.append('component or parameter')
        if re.search(r'\bEffectSlot\s+\w+\s*[;=]', code):
            kinds.append('field or local')
        types.append((str(path.relative_to(root)).replace('/', '.')[:-5].replace(
            'com.github.laxika.magicalvibes.', ''), ', '.join(kinds) or 'other'))
    return sorted(types)


def runtime_slot_state():
    """Fields keyed by EffectSlot on the runtime model classes (not Card, which step 3 handles)."""
    field = re.compile(r'^    (?:(?:private|public|protected|static|final|transient|volatile)\s+)*'
                       r'(\w[\w.<>, ?]*\bEffectSlot\b[\w.<>, ?]*?)\s+(\w+)\s*[=;]')
    state = []
    for cls in ('GameData', 'Permanent', 'StackEntry', 'TemporaryGlobalTriggeredAbility', 'Emblem', 'PlanarObject'):
        for path in (ROOT / 'magical-vibes-domain/src/main/java').glob(f'**/{cls}.java'):
            for line in strip_java(path.read_text()).splitlines():
                m = field.match(line)
                if m:
                    state.append(f'{cls}.{m.group(2)}: {" ".join(m.group(1).split())}')
    return sorted(set(state))


def load_classification():
    rows = collections.OrderedDict()
    if not CLASSIFICATION.exists():
        return rows
    with CLASSIFICATION.open() as f:
        for row in csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE):
            if list(row) != TSV_COLUMNS:
                sys.exit(f'{CLASSIFICATION.name}: expected the columns {TSV_COLUMNS}')
            rows[row['constant']] = row
    return rows


def card_test_classes():
    """Card class simple name -> fully qualified card test classes named after it."""
    tests = collections.defaultdict(list)
    for path in (ROOT / 'magical-vibes-card-test').glob('*/src/test/java/**/*Test.java'):
        rel = path.relative_to(ROOT / 'magical-vibes-card-test')
        module = rel.parts[0]
        fqcn = '.'.join(rel.parts[4:])[:-5]
        tests[path.stem[:-4]].append((module, fqcn))
    return tests


def junit_results(pattern):
    """Fully qualified test classes that ran and the ones that failed, from JUnit XML reports."""
    ran, failed, times = set(), set(), []
    for path in glob.glob(pattern):
        times.append(os.path.getmtime(path))
        with open(path, errors='replace') as f:
            head = f.read(4096)
        m = re.search(r'<testsuite\b[^>]*>', head)
        if not m:
            continue
        attrs = dict(re.findall(r'(\w+)="([^"]*)"', m.group(0)))
        ran.add(attrs['name'])
        if int(attrs.get('failures', 0)) + int(attrs.get('errors', 0)):
            failed.add(attrs['name'])
    stamp = lambda t: datetime.datetime.fromtimestamp(t).strftime('%Y-%m-%d %H:%M')
    span = f'{stamp(min(times))} to {stamp(max(times))}' if times else 'none'
    return ran, failed, span


def build():
    constants = parse_slots()
    slot_names = [c for c, _ in constants]
    wrappers, trigger_effects, aware, aware_names = effect_class_info()
    cards = scan_cards(slot_names, wrappers, trigger_effects, aware)
    refs, generic, dispatchers = scan_readers(slot_names, aware_names)
    classification = load_classification()
    commit = subprocess.run(['git', 'rev-parse', '--short=10', 'HEAD'], cwd=ROOT, capture_output=True,
                            text=True).stdout.strip()
    zone_change_methods = set()
    for name in slot_names:
        row = classification.get(name)
        if row and row['family'] in ('10', '11', '12'):
            zone_change_methods |= {r['name'] for r in refs.get(name, ()) if r['how'] != 'handler' and r['name']
                                    and r['module'] == 'engine' and r['name'] not in ('<init>', '<clinit>')}
    callers = scan_callers(zone_change_methods)
    data = {'commit': commit, 'constants': []}
    for name, doc in constants:
        c = cards[name]
        readers = refs.get(name, [])
        bound = set().union(*[set(r['aware']) for r in readers]) if readers else set()
        data['constants'].append({
            'name': name, 'doc': doc,
            'cards': sorted(c['cards']), 'card_count': len(c['cards']),
            'multi_registration_cards': sorted(c['multi']),
            'modes': dict(c['modes']), 'simultaneous': c['simultaneous'],
            'outer': dict(c['outer']), 'wrappers': dict(c['wrappers']), 'trigger_effects': dict(c['triggers']),
            'aware': {a: n for a, n in c['aware'].items() if a in bound}, 'unresolved': c['unresolved'],
            'readers': readers,
            'callers': {r['method']: sorted(callers.get(r['name'], ())) for r in readers
                        if r['name'] in zone_change_methods},
            'classification': classification.get(name),
        })
    data['generic_readers'] = generic
    data['dispatchers'] = dispatchers
    data['wrapper_classes'] = sorted(wrappers)
    data['aware_interfaces'] = sorted(aware_names)
    data['domain_types'] = domain_slot_types()
    data['runtime_state'] = runtime_slot_state()
    return data


def step_of(c):
    row = c['classification']
    return row['family'] if row else '?'


def test_classes_by_step(data):
    tests = card_test_classes()
    by_step = collections.OrderedDict((step, {'cards': set(), 'tests': set()}) for step in FAMILIES)
    by_step.setdefault('?', {'cards': set(), 'tests': set()})
    for c in data['constants']:
        bucket = by_step[step_of(c)]
        bucket['cards'] |= set(c['cards'])
        for card in c['cards']:
            bucket['tests'] |= set(tests.get(card, ()))
    return by_step


def gradle_args(data, step):
    by_module = collections.defaultdict(list)
    for module, fqcn in sorted(test_classes_by_step(data)[step]['tests']):
        by_module[module].append(fqcn)
    args = []
    for module, classes in sorted(by_module.items()):
        args.append(f':magical-vibes-card-test:{module}:test')
        args += [f'--tests {fqcn}' for fqcn in classes]
    return ' '.join(args)


def md(text):
    return (text or '').replace('|', '\\|').replace('\n', ' ').strip()


def counted(counter, limit=8):
    items = sorted(counter.items(), key=lambda kv: (-kv[1], kv[0]))
    text = ', '.join(f'`{k}` {v}' for k, v in items[:limit])
    if len(items) > limit:
        text += f', +{len(items) - limit} more'
    return text


def readers_text(readers):
    by_class = collections.OrderedDict()
    for r in readers:
        cls, _, method = r['method'].partition('#')
        entry = by_class.setdefault(cls, {'methods': collections.OrderedDict(), 'handlers': []})
        if r['how'] == 'handler':
            entry['handlers'].append(r['handles'] or '?')
        elif method:
            guess = r['group'] if r['how'] == 'ref' else f"{r['group']}, {r['how']}"
            entry['methods'].setdefault(method, guess)
        else:
            entry['methods'].setdefault('(field)', 'read')
    parts = []
    for cls, entry in by_class.items():
        items = [f'`{m}` ({g})' for m, g in entry['methods'].items()]
        if entry['handlers']:
            handled = collections.Counter(entry['handlers'])
            names = ', '.join(f'`{h}`' + (f' ×{n}' if n > 1 else '') for h, n in sorted(handled.items()))
            items.append(f"{len(entry['handlers'])} `@CollectsTrigger` handler(s) for {names}")
        parts.append(f'`{cls}`: ' + ', '.join(items))
    return '; '.join(parts)


def render(data, args):
    lines = []
    out = lines.append
    constants = data['constants']
    results = None
    if args.results:
        results = junit_results(args.results)
    by_step = test_classes_by_step(data)

    out('# EffectSlot inventory')
    out('')
    out('Step 1 of [effect-slot-refactor.md](effect-slot-refactor.md): one row per `EffectSlot` constant. '
        f'Generated by `scripts/effect-slot-inventory.py` at `{data["commit"]}`; do not edit it by hand. '
        'The kind, step, event, object, relation, zone, multiplicity, self, grouping and notes columns '
        'come from `scripts/effect-slot-classification.tsv`, which is kept by hand and was checked against '
        'the collectors. Everything else is read from the sources on every run.')
    out('')
    out('Re-run `python3 scripts/effect-slot-inventory.py` after a step moves slots (add `--results` after '
        'a full card-test run to refresh the baseline), and update the TSV when a slot changes behaviour. '
        '`--gradle-args <step>` prints the Gradle arguments that run the card test classes of a step\'s cards, '
        'one `--tests` filter per class after each module\'s test task.')
    out('')
    out('## Columns')
    out('')
    out('- **Kind**: spell, static, activated, mana, replacement, special action, marker or triggered '
        '(CR 113.3). A slot that mixes kinds is listed under its main kind; its notes name the rest.')
    out('- **Step**: the refactor step that moves the constant. 13.x is row x of step 13\'s table.')
    out('- **Event**, **Object**, **Relation**: what happens, the type filter on the object it happens to, and '
        'who that object or player is relative to the source and its controller, as the code checks them.')
    out('- **Zone**: where the source has to be for the ability to work.')
    out('- **Mult.**: how often one event triggers it: per object, per event (one or more), per counter, ….')
    out('- **Self**: whether the source\'s own event triggers it: only, yes, no, heuristic, or n/a.')
    out('- **Grouping**: how the readers turn several registrations of the slot on one card into stack '
        'objects. *each*: one stack object per registration. *all*: one stack object for all of them. '
        '*all-but-independent*: one shared object, plus one for each `TriggerMode.INDEPENDENT` '
        'registration. *split*: partitioned some other way (see notes). *none*: never put on the stack. '
        '*varies*: the readers disagree (see notes).')
    out('- **Cards**: card classes in `magical-vibes-card` that name the constant.')
    out('')
    out('The details under each table give, per constant:')
    out('')
    out('- **Readers**: every method in engine, domain, AI, networking and engine test-fixture main code that '
        'names the constant (`EffectSlot.X`, a `@CollectsTrigger` slot, a `case` of a `switch` on a slot, or '
        'a `slot.name()` prefix test), by class, with a guess from the method body: *each* (dispatches '
        'each registration through the registry or queues `List.of(effect)`), *all* (queues the whole '
        'list), *mode* (reads `TriggerMode.INDEPENDENT`), *mixed* (both), *read* (does not queue), '
        '*queue?* (queues, shape unclear). The Grouping column is the reviewed answer. A handler gets one '
        'registration at a time from `TriggerCollectorRegistry`, so it is listed with the effect class it '
        'is registered for. Methods that walk `EffectSlot.values()` are listed once, under '
        '[Readers of every slot](#readers-of-every-slot).')
    out('- **Wrappers**: trigger-level wrapper effects (`May…`, `…Conditional…`, `Once…`, `SequenceEffect`, '
        '`ChooseOne…`) in the card registrations, with counts. **Trigger effects**: `…TriggerEffect` classes. '
        '**Aware**: `…AwareEffect` interfaces that the slot\'s readers bind, with the number of '
        'registrations whose effects implement them. Registrations built in helper methods are not '
        'resolved.')
    out('- **Modes**: registrations with a `TriggerMode` other than `NORMAL`, and `addSimultaneousEffects` '
        'calls.')
    out('- **Zone-change choke points** (steps 10 to 12): the callers of each reader method.')
    out('')

    out('## Summary')
    out('')
    out('| Step | Constants | Card classes | Card test classes |' + (' Failing at HEAD |' if results else ''))
    out('|---|---|---|---|' + ('---|' if results else ''))
    for step, title in FAMILIES.items():
        members = [c for c in constants if step_of(c) == step]
        bucket = by_step[step]
        row = f'| [{title}](#{anchor(title)}) | {len(members)} | {len(bucket["cards"])} | {len(bucket["tests"])} |'
        if results:
            row += f' {sum(1 for _, t in bucket["tests"] if t in results[1])} |'
        out(row)
    unclassified = [c['name'] for c in constants if step_of(c) == '?']
    if unclassified:
        out(f'| Unclassified | {len(unclassified)} | | |' + (' |' if results else ''))
    out('')
    kinds = collections.Counter(c['classification']['kind'] for c in constants if c['classification'])
    out('By kind: ' + ', '.join(f'{k} {kinds[k]}' for k in KINDS if kinds[k]) + '. '
        f'{sum(1 for c in constants if c["card_count"] == 0)} constants are used by no card class, '
        f'{sum(1 for c in constants if c["card_count"] == 1)} by exactly one.')
    out('')

    for step, title in list(FAMILIES.items()) + [('?', 'Unclassified')]:
        members = [c for c in constants if step_of(c) == step]
        if not members:
            continue
        out(f'## {title}')
        out('')
        out('| Constant | Kind | Event | Object | Relation | Zone | Mult. | Self | Grouping | Cards |')
        out('|---|---|---|---|---|---|---|---|---|---|')
        for c in members:
            r = c['classification'] or collections.defaultdict(str)
            out(f'| `{c["name"]}` | {md(r["kind"])} | {md(r["event"])} | {md(r["object"])} | '
                f'{md(r["relation"])} | {md(r["zone"])} | {md(r["multiplicity"])} | {md(r["self"])} | '
                f'{md(r["grouping"])} | {c["card_count"]} |')
        out('')
        for c in members:
            r = c['classification'] or collections.defaultdict(str)
            details = []
            details.append('Readers: ' + (readers_text(c['readers']) or 'none'))
            if c['wrappers']:
                details.append('Wrappers: ' + counted(c['wrappers']))
            if c['trigger_effects']:
                details.append('Trigger effects: ' + counted(c['trigger_effects']))
            if c['aware']:
                details.append('Aware: ' + counted(c['aware']))
            modes = {k: v for k, v in c['modes'].items() if k != 'NORMAL'}
            if modes or c['simultaneous']:
                text = ', '.join(f'`{k}` {v}' for k, v in sorted(modes.items()))
                if c['simultaneous']:
                    text += (', ' if text else '') + f'simultaneous {c["simultaneous"]}'
                details.append('Modes: ' + text)
            if len(c['multi_registration_cards']):
                details.append(f'{len(c["multi_registration_cards"])} card(s) register 2+ effects in it')
            if c['callers']:
                parts = []
                for method, sites in c['callers'].items():
                    shown = ', '.join(f'`{s}`' for s in sites[:12])
                    more = f', +{len(sites) - 12} more' if len(sites) > 12 else ''
                    parts.append(f'`{method}` ← {shown or "no engine caller"}{more}')
                details.append('Choke points: ' + '; '.join(parts))
            if r['notes']:
                details.append('Notes: ' + md(r['notes']))
            out(f'- **`{c["name"]}`**. ' + '. '.join(d.rstrip('. ') for d in details) + '.')
        out('')

    out('## Readers of every slot')
    out('')
    out('These methods walk `EffectSlot.values()`, so they read every constant: copy, mutation, '
        'ability-granting and printed-ability checks.')
    out('')
    for module, method in data['generic_readers']:
        out(f'- `{method}` ({module})')
    out('')
    out('## Domain types that carry an EffectSlot')
    out('')
    out(f'{len(data["domain_types"])} types in `magical-vibes-domain` other than `Card` and `EffectSlot` hold '
        'or take an `EffectSlot` (step 4 converts the ones that hold triggered abilities):')
    out('')
    for name, kinds in data['domain_types']:
        out(f'- `{name}`: {kinds}')
    out('')
    out('## Runtime state keyed by EffectSlot')
    out('')
    for field in data['runtime_state']:
        out(f'- `{field}`')
    out('')
    out('## Baseline')
    out('')
    if args.all_passed:
        out(f'Full card-test run at `{data["commit"]}`: every card test class passed, as reported by the user '
            '(the JUnit reports were not on this machine). So "fail exactly as at the baseline" means that no '
            'card test fails. Card test classes of each step\'s cards:')
        out('')
        out('| Step | Card test classes | Failing |')
        out('|---|---|---|')
        for step, title in FAMILIES.items():
            out(f'| {title} | {len(by_step[step]["tests"])} | 0 |')
        out('')
    elif results:
        ran, failed, span = results
        out(f'Full card-test run at `{data["commit"]}` (reports written {span}): {len(ran)} card test classes '
            f'ran, {len(failed)} failed. The card test classes of each step\'s cards that failed:')
        out('')
        for step, title in FAMILIES.items():
            tests = by_step[step]['tests']
            failing = sorted(t for _, t in tests if t in failed)
            missing = sum(1 for _, t in tests if t not in ran)
            out(f'### {title}')
            out('')
            out(f'{len(tests)} test classes, {len(failing)} failing' +
                (f', {missing} not in the reports' if missing else '') + '.')
            out('')
            for t in failing:
                out(f'- `{t.rsplit(".", 1)[-1]}`')
            if failing:
                out('')
    else:
        out('Not recorded yet. Run the full card-test suite at this commit, then '
            '`python3 scripts/effect-slot-inventory.py --results` to list the failing card test classes per '
            'step.')
        out('')

    OUTPUT.write_text('\n'.join(lines).rstrip() + '\n')


def anchor(title):
    return re.sub(r'[^a-z0-9 -]', '', title.lower()).replace(' ', '-')


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--json', help='dump the raw data to this file')
    parser.add_argument('--results', nargs='?',
                        const=str(ROOT / 'magical-vibes-card-test/*/build/test-results/test/*.xml'),
                        help='glob of the JUnit XML reports of a full card-test run at this commit')
    parser.add_argument('--all-passed', action='store_true',
                        help='record a full card-test run at this commit in which every card test class passed')
    parser.add_argument('--gradle-args', metavar='STEP', help='print the Gradle arguments for a step\'s card tests')
    parser.add_argument('--check', action='store_true', help='fail when the classification misses a constant')
    parser.add_argument('--no-write', action='store_true', help='do not rewrite effect-slot-inventory.md')
    args = parser.parse_args()
    data = build()
    if args.json:
        Path(args.json).write_text(json.dumps(data, indent=1))
    if args.gradle_args:
        print(gradle_args(data, args.gradle_args))
        return
    missing = [c['name'] for c in data['constants'] if not c['classification']]
    extra = set(load_classification()) - {c['name'] for c in data['constants']}
    if args.check and (missing or extra):
        print('unclassified: ' + ', '.join(missing) + '\nnot in EffectSlot: ' + ', '.join(sorted(extra)),
              file=sys.stderr)
        sys.exit(1)
    if not args.no_write:
        render(data, args)


if __name__ == '__main__':
    main()
