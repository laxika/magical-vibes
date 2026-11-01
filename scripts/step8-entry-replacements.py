#!/usr/bin/env python3
"""EffectSlot refactor step 8: replacement effects, special actions and markers leave EffectSlot.

Usage: step8-entry-replacements.py --scan SCAN.tsv DIR...

Rewrites every card class (.java) under each DIR (run it per letter module and on ``shared``):

- The slots that hold no triggered ability become static abilities, each functioning from the zones
  its effect names (``CardEffect.staticAbilityZones()``):
  ``addEffect(EffectSlot.GRAVEYARD_DRAW_REPLACEMENT | MAY_SKIP_DRAW_STEP_DRAW | MULLIGAN_ACTION |
  ON_OPENING_HAND_REVEAL, x)`` becomes ``addStaticEffect(x)``, and so does Bell Borca's
  ``addEffect(EffectSlot.ON_ANY_CARD_EXILED, new NoteManaValueOfExiledCardsEffect())``.
  ``addEffect(EffectSlot.GRANT_<KIND>, x)`` becomes
  ``addStaticEffect(new GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant.<KIND>, x))``.
- An "enters with" or "as enters" effect (CR 614.1c) registered in ``ON_ENTER_BATTLEFIELD`` becomes a static
  ability: ``addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, x)``, also in a ``target(...)`` chain, becomes
  ``addStaticEffect(x)``, and ``LegacySlots.triggered(EffectSlot.ON_ENTER_BATTLEFIELD, x)`` in a token's
  ability list becomes ``StaticAbility.of(x)``. Which effects those are comes from the runtime scan
  (``scripts/step8-etb-scan.java``, run on the card jars at the revision before this step): its ``R`` rows are
  the effects ``EtbTriggerService`` filtered out of the trigger as entry replacements, plus the
  ``EXTRA_ENTRY_REPLACEMENTS`` below, as-enters choices the engine used to file as triggers.

  The script finds each call site's effect class from its source: ``new X(...)``, ``X.factory(...)``, a
  ``ConditionalEffect`` / ``ConditionalReplacementEffect`` around one, or a local variable, field or helper
  method of the card that returns one. A card is converted only when the number of entry replacements it finds
  equals the number the scan found for that card; any other card is reported and left alone.

With ``--readers DIR...`` it rewrites engine, AI and networking sources instead: a statement that reads
``getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)`` and names only entry-replacement types (the scan's classes and
the as-enters interfaces in ``ENTRY_REPLACEMENT_INTERFACES``) reads ``getStaticEffects()`` instead. Every other
reader of the slot is listed with the types it names, to be converted or kept by hand.

With ``--by-class DIR...`` (test sources, whose fixture cards the scan doesn't know) an
``ON_ENTER_BATTLEFIELD`` registration is converted when its effect class is an entry replacement, without
the count check.

Imports that a rewrite needs are added and an ``EffectSlot`` or ``LegacySlots`` import left unused is
removed. Prints every file it changed and every card it could not convert.
"""
import collections
import pathlib
import re
import sys

MODEL = 'com.github.laxika.magicalvibes.model'
SLOT = r'(?:com\.github\.laxika\.magicalvibes\.model\.)?EffectSlot\.'
STATIC_SLOTS = ['GRAVEYARD_DRAW_REPLACEMENT', 'MAY_SKIP_DRAW_STEP_DRAW', 'MULLIGAN_ACTION', 'ON_OPENING_HAND_REVEAL']
GRANT_SLOTS = [
    'STORM_TO_INSTANT_OR_SORCERY', 'CASCADE_TO_FIRST_SPELL', 'CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA',
    'CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND', 'CASCADE_TO_ENCHANTMENT_FROM_HAND', 'CASCADE_TO_SLIVER_SPELL',
    'DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND',
    'CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST',
]
# "As this enters, choose ..." effects (CR 614.1c) that the engine read in place as the permanent entered but
# also left in the enters trigger, which put an ability with nothing to do on the stack.
EXTRA_ENTRY_REPLACEMENTS = {
    'ChooseNumberOnEnterEffect',
    'ChooseAnotherCreatureOnEnterEffect',
    'YouAndOpponentChooseCardNamesOnEnterEffect',
    'ChooseOpponentPermanentOnEnterEffect',
}
# The Mystical Archive: "As this land enters, draft a card ...". The effect is an entry replacement only on a
# land; the scan builds cards without oracle data, so it can't see the type.
LAND_ONLY = {'TheMysticalArchive': 'DraftCardFromSpellbookToExileEffect'}


# As-enters interfaces whose implementers all are entry replacements once ProtectionFromChosenColorEffect is no
# longer a ChooseColorEffect.
ENTRY_REPLACEMENT_INTERFACES = {
    'ChooseColorEffect', 'SubtypeChoiceOnEnterEffect', 'NumberChoiceEffect', 'TwoPlayerChoiceOnEnterEffect',
    'PowerToughnessFormChoiceEffect',
}
# Types a reader may name next to an entry replacement without making the statement about anything else.
NEUTRAL_TYPES = {
    'CardEffect', 'ConditionalEffect', 'List', 'Optional', 'Objects', 'Stream', 'Collectors', 'String', 'Integer',
    'UUID', 'Permanent', 'Card', 'Set', 'ArrayList', 'Condition',
}


def read_scan(path):
    """Card class -> number of ON_ENTER_BATTLEFIELD registrations that are entry replacements; and their classes."""
    counts = collections.Counter()
    classes = set()
    for line in open(path, encoding='utf-8'):
        parts = line.rstrip('\n').split('\t')
        if parts[0] != 'ETB':
            continue
        card, kind, effect = parts[2], parts[4], parts[5]
        leaf = re.sub(r'^Conditional(?:Replacement)?Effect\((?:-,)?', '', effect).rstrip(')')
        if kind == 'R' or leaf in EXTRA_ENTRY_REPLACEMENTS or LAND_ONLY.get(card) == leaf:
            counts[card] += 1
            classes.add(leaf.split(',')[-1])
    return counts, classes | EXTRA_ENTRY_REPLACEMENTS


def balanced(text, start):
    """Index just past the parenthesis group opening at text[start] == '(', skipping strings and comments."""
    depth = 0
    i = start
    while i < len(text):
        ch = text[i]
        if text.startswith('//', i):
            i = text.index('\n', i)
            continue
        if text.startswith('/*', i):
            i = text.index('*/', i) + 2
            continue
        if ch == '"':
            i += 1
            while text[i] != '"':
                i += 2 if text[i] == '\\' else 1
        elif ch == "'":
            i += 1
            while text[i] != "'":
                i += 2 if text[i] == '\\' else 1
        elif ch == '(':
            depth += 1
        elif ch == ')':
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1
    raise ValueError('unbalanced')


def split_args(inner):
    inner = strip_comments(inner)
    args, depth, cur, in_str = [], 0, '', None
    i = 0
    while i < len(inner):
        ch = inner[i]
        if in_str:
            cur += ch
            if ch == '\\':
                cur += inner[i + 1]
                i += 2
                continue
            if ch == in_str:
                in_str = None
        elif ch in '"\'':
            in_str = ch
            cur += ch
        elif ch in '([{':
            depth += 1
            cur += ch
        elif ch in ')]}':
            depth -= 1
            cur += ch
        elif ch == ',' and depth == 0:
            args.append(cur.strip())
            cur = ''
        else:
            cur += ch
        i += 1
    if cur.strip():
        args.append(cur.strip())
    return args


def strip_comments(expr):
    expr = re.sub(r'//[^\n]*', '', expr)
    return re.sub(r'/\*.*?\*/', '', expr, flags=re.S).strip()


def effect_class(expr, source, rclasses, depth=0):
    """The class of the effect an expression builds, unwrapping conditionals; None if unknown."""
    expr = strip_comments(expr)
    if depth > 4:
        return None
    m = re.match(r'new\s+(?:[\w.]+\.)?(\w+)\s*(?:<[^>]*>)?\s*\(', expr)
    if m:
        name = m.group(1)
        if name in ('ConditionalEffect', 'ConditionalReplacementEffect'):
            open_paren = expr.index('(', m.start())
            args = split_args(expr[open_paren + 1:balanced(expr, open_paren) - 1])
            kinds = {effect_class(a, source, rclasses, depth + 1) for a in args[1:] if a != 'null'}
            if kinds and kinds <= rclasses:
                return min(kinds)
            return None if None in kinds else name
        return name
    m = re.match(r'([A-Z]\w*)\.\w+\s*\(', expr)
    if m:
        return m.group(1)
    m = re.match(r'(?:this\.)?([a-z]\w*)\s*(\(.*\))?$', expr, re.S)
    if m:
        name = m.group(1)
        if m.group(2) is not None:
            body = re.search(r'\b\w[\w<>, ]*\s+' + name + r'\s*\([^)]*\)\s*\{(.*?)\n    \}', source, re.S)
            ret = body and re.search(r'return\s+(.*?);', body.group(1), re.S)
            return effect_class(ret.group(1), source, rclasses, depth + 1) if ret else None
        decl = re.search(r'\b[\w<>?, .]+\s+' + name + r'\s*=\s*(.*?);', source, re.S)
        return effect_class(decl.group(1), source, rclasses, depth + 1) if decl else None
    return None


def add_import(text, fqn):
    """Adds an import in alphabetical place among the project's own imports."""
    if re.search(r'^import\s+' + re.escape(fqn) + r';', text, re.M):
        return text
    own = [m for m in re.finditer(r'^import (com\.github\.laxika\.[^;]+);\n', text, re.M)]
    later = [m for m in own if m.group(1) > fqn]
    if later:
        at = later[0].start()
    elif own:
        at = own[-1].end()
    else:
        at = text.index('\n', text.index('package')) + 1
    return text[:at] + 'import ' + fqn + ';\n' + text[at:]


def drop_unused_import(text, simple, fqn):
    body = re.sub(r'^import [^;]+;\n', '', text, flags=re.M)
    if not re.search(r'\b' + simple + r'\b', body):
        text = re.sub(r'^import ' + re.escape(fqn) + r';\n', '', text, flags=re.M)
    return text


def convert_small_slots(text):
    changed = False
    for slot in STATIC_SLOTS:
        text, n = re.subn(r'addEffect\(\s*' + SLOT + slot + r'\s*,\s*', 'addStaticEffect(', text)
        changed |= n > 0
    text, n = re.subn(r'addEffect\(\s*' + SLOT + r'ON_ANY_CARD_EXILED\s*,\s*(new NoteManaValueOfExiledCardsEffect\(\))',
                      r'addStaticEffect(\1', text)
    changed |= n > 0
    for kind in GRANT_SLOTS:
        pattern = re.compile(r'addEffect\(\s*' + SLOT + 'GRANT_' + kind + r'\s*,\s*')
        while True:
            m = pattern.search(text)
            if not m:
                break
            open_paren = text.index('(', m.start())
            end = balanced(text, open_paren)
            effect = text[m.end():end - 1].strip()
            indent = re.match(r'[ \t]*', text[text.rindex('\n', 0, m.start()) + 1:]).group(0)
            text = (text[:m.start()] + 'addStaticEffect(new GrantCastTriggerToSpellsEffect(\n' + indent
                    + '        SpellCastTriggerGrant.' + kind + ', ' + effect + '))' + text[end:])
            text = add_import(text, MODEL + '.effect.GrantCastTriggerToSpellsEffect')
            text = add_import(text, MODEL + '.effect.SpellCastTriggerGrant')
            changed = True
    return text, changed


def convert_entry_replacements(text, card, expected, rclasses, report, by_class=False):
    sites = []
    for m in re.finditer(r'(addEffect|addSimultaneousEffects)\(\s*' + SLOT + r'ON_ENTER_BATTLEFIELD\s*,', text):
        open_paren = text.index('(', m.start())
        end = balanced(text, open_paren)
        args = split_args(text[open_paren + 1:end - 1])[1:]
        if m.group(1) == 'addSimultaneousEffects':
            kinds = [effect_class(a, text, rclasses) for a in args]
            if any(k in rclasses for k in kinds) or None in kinds:
                sites.append(('sim', m, end, None, kinds))
            continue
        cls = effect_class(args[0], text, rclasses)
        sites.append(('add', m, end, args, cls))
    for m in re.finditer(r'LegacySlots\.triggered\(\s*' + SLOT + r'ON_ENTER_BATTLEFIELD\s*,', text):
        open_paren = text.index('(', m.start())
        end = balanced(text, open_paren)
        args = split_args(text[open_paren + 1:end - 1])[1:]
        sites.append(('token', m, end, args, effect_class(args[0], text, rclasses)))
    # The scan counts the card's own registrations; a token's ability list is checked by its class alone.
    hits = [s for s in sites if s[0] != 'sim' and s[4] in rclasses]
    own = [s for s in hits if s[0] == 'add']
    unknown = [s for s in sites if s[0] == 'sim' or s[4] is None]
    if by_class:
        expected = len(own)
        unknown = []
    if len(own) != expected or (unknown and len(own) < expected):
        if expected or own:
            report.append(f'{card}: scan found {expected} entry replacement(s), source {len(own)}'
                          + (f', {len(unknown)} unresolved site(s)' if unknown else ''))
        return text, False
    for kind, m, end, args, cls in sorted(hits, key=lambda s: s[1].start(), reverse=True):
        if kind == 'add':
            if len(args) != 1:
                report.append(f'{card}: an entry replacement with a trigger mode ({cls})')
                return text, False
            text = text[:m.start()] + 'addStaticEffect(' + args[0] + ')' + text[end:]
        else:
            text = text[:m.start()] + 'StaticAbility.of(' + args[0] + ')' + text[end:]
            text = add_import(text, MODEL + '.StaticAbility')
    return text, bool(hits)


def statement_at(text, pos):
    """The statement around pos: from the previous ';', '{' or '}' to its ';', or a whole for loop."""
    start = max(text.rfind(';', 0, pos), text.rfind('{', 0, pos), text.rfind('}', 0, pos)) + 1
    head = text[start:pos]
    if re.match(r'\s*for\s*\(', head):
        brace = text.index('{', pos)
        depth = 0
        for i in range(brace, len(text)):
            depth += {'{': 1, '}': -1}.get(text[i], 0)
            if depth == 0:
                return start, i + 1
    depth = 0
    i = start
    while i < len(text):
        depth += {'(': 1, ')': -1, '{': 1, '}': -1}.get(text[i], 0)
        if text[i] == ';' and depth == 0:
            return start, i + 1
        i += 1
    return start, len(text)


def named_types(statement):
    names = set(re.findall(r'instanceof\s+(?:[\w.]+\.)?([A-Z]\w*)', statement))
    names |= set(re.findall(r'(?:[\w.]+\.)?([A-Z]\w*)\.class\b', statement))
    names |= set(re.findall(r'\(\s*(?:[\w.]+\.)?([A-Z]\w*)\s*\)\s*[\w(]', statement))
    names |= set(re.findall(r'^\s*(?:final\s+)?(?:[\w.]+\.)?([A-Z]\w*)(?:<[^>]*>)?\s+\w+\s*=', statement))
    names |= set(re.findall(r'\b([A-Z]\w*)::\w+', statement))
    return names


# Entry replacements some cards already registered as static abilities, and the protection that used to double as
# Voice of All's colour choice: their readers already see static abilities, or must tell scopes apart, so a
# person converts them.
BY_HAND_TYPES = {
    'ProtectionFromChosenColorEffect', 'EnterWithCountersEffect', 'BecomePreparedEffect', 'ChooseOpponentOnEnterEffect',
}


def convert_readers(dirs, rclasses):
    entry_types = (rclasses | ENTRY_REPLACEMENT_INTERFACES) - BY_HAND_TYPES
    read = re.compile(r'getEffects\(\s*' + SLOT + r'ON_ENTER_BATTLEFIELD\s*\)')
    for root in dirs:
        for path in sorted(pathlib.Path(root).rglob('*.java')):
            text = path.read_text(encoding='utf-8')
            original = text
            pos = 0
            while True:
                m = read.search(text, pos)
                if not m:
                    break
                start, end = statement_at(text, m.start())
                statement = text[start:end]
                types = named_types(statement) - NEUTRAL_TYPES
                line = text.count('\n', 0, m.start()) + 1
                if types and types <= entry_types and 'getStaticEffects' not in statement:
                    text = text[:m.start()] + 'getStaticEffects()' + text[m.end():]
                    pos = m.start() + len('getStaticEffects()')
                    print(f'converted {path.name}:{line} {sorted(types)}')
                else:
                    pos = m.end()
                    print(f'KEPT {path.name}:{line} {sorted(types)}')
            if text != original:
                text = drop_unused_import(text, 'EffectSlot', MODEL + '.EffectSlot')
                path.write_text(text, encoding='utf-8')


def main(argv):
    if '--scan' not in argv:
        sys.exit(__doc__)
    i = argv.index('--scan')
    counts, rclasses = read_scan(argv[i + 1])
    dirs = argv[:i] + argv[i + 2:]
    by_class = '--by-class' in dirs
    if by_class:
        dirs.remove('--by-class')
    if '--readers' in dirs:
        dirs.remove('--readers')
        convert_readers(dirs, rclasses)
        return
    report = []
    for root in dirs:
        for path in sorted(pathlib.Path(root).rglob('*.java')):
            text = path.read_text(encoding='utf-8')
            original = text
            text, _ = convert_small_slots(text)
            try:
                text, _ = convert_entry_replacements(text, path.stem, counts.get(path.stem, 0), rclasses, report,
                                                     by_class)
            except ValueError as error:
                report.append(f'{path.stem}: could not parse ({error})')
            if text != original:
                text = drop_unused_import(text, 'EffectSlot', MODEL + '.EffectSlot')
                text = drop_unused_import(text, 'LegacySlots', MODEL + '.LegacySlots')
                path.write_text(text, encoding='utf-8')
                print('changed', path)
    for line in report:
        print('NOT CONVERTED', line)


if __name__ == '__main__':
    main(sys.argv[1:])
