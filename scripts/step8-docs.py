#!/usr/bin/env python3
"""EffectSlot refactor step 8: rewrites agent-docs snippets for the slots that left EffectSlot.

Usage: step8-docs.py --scan SCAN.tsv FILE...

- An "enters with" or "as enters" effect (the classes ``step8-entry-replacements.py`` moves) is a static
  ability: ``addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, x)`` becomes ``addStaticEffect(x)``, and the tables'
  ``ON_ENTER_BATTLEFIELD x(...)`` shorthand becomes ``STATIC x(...)``.
- ``GRAVEYARD_DRAW_REPLACEMENT``, ``MAY_SKIP_DRAW_STEP_DRAW``, ``MULLIGAN_ACTION`` and ``ON_OPENING_HAND_REVEAL``
  registrations become ``addStaticEffect(x)`` and their shorthand ``STATIC x``; a ``GRANT_<KIND>`` registration
  becomes ``addStaticEffect(new GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant.<KIND>, x))``.

Prose that names those slots is left for a person to edit; the script prints every line that still names one.
"""
import importlib.util
import pathlib
import re
import sys

HERE = pathlib.Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('step8', HERE / 'step8-entry-replacements.py')
step8 = importlib.util.module_from_spec(spec)
spec.loader.exec_module(step8)

OLD_SLOTS = step8.STATIC_SLOTS + ['GRANT_' + kind for kind in step8.GRANT_SLOTS]


def rewrite(text, rclasses):
    def code(match):
        open_paren = text.index('(', match.start())
        try:
            end = step8.balanced(text, open_paren)
        except (ValueError, IndexError):
            return None
        args = step8.split_args(text[open_paren + 1:end - 1])[1:]
        if len(args) == 1 and step8.effect_class(args[0], '', rclasses) in rclasses:
            return match.start(), end, 'addStaticEffect(' + args[0] + ')'
        return None

    pattern = re.compile(r'addEffect\(\s*(?:EffectSlot\.)?ON_ENTER_BATTLEFIELD\s*,')
    pos = 0
    while True:
        m = pattern.search(text, pos)
        if not m:
            break
        hit = code(m)
        if hit:
            text = text[:hit[0]] + hit[2] + text[hit[1]:]
            pos = hit[0] + len(hit[2])
        else:
            pos = m.end()
    names = '|'.join(sorted(rclasses, key=len, reverse=True))
    text = re.sub(r'`ON_ENTER_BATTLEFIELD ((?:ConditionalEffect\([^`]*?)?(?:new )?(?:' + names + r')\b)',
                  r'`STATIC \1', text)
    text = re.sub(r'\bON_ENTER_BATTLEFIELD ((?:new )?(?:' + names + r')\()', r'STATIC \1', text)
    # The tables' other shorthands: "ON_ENTER_BATTLEFIELD x", "`ON_ENTER_BATTLEFIELD` + `x(...)`" and a conditional
    # around one.
    text = re.sub(r'ON_ENTER_BATTLEFIELD(?=`?\s*(?:\+\s*)?`?\s*(?:new\s+)?(?:ConditionalEffect\([^`|]{0,80}?)?(?:'
                  + names + r')\b)', 'STATIC', text)
    text, _ = step8.convert_small_slots(text)
    for slot in step8.STATIC_SLOTS:
        text = re.sub(r'\b(?:EffectSlot\.)?' + slot + r' (?=(?:new )?[A-Z]\w*\()', 'STATIC ', text)
    for kind in step8.GRANT_SLOTS:
        text = re.sub(r'`(?:EffectSlot\.)?GRANT_' + kind + r' ([^`]*)`',
                      r'`STATIC GrantCastTriggerToSpellsEffect(SpellCastTriggerGrant.' + kind + r', \1)`', text)
    return text


def main(argv):
    i = argv.index('--scan')
    _, rclasses = step8.read_scan(argv[i + 1])
    for name in argv[:i] + argv[i + 2:]:
        path = pathlib.Path(name)
        text = path.read_text(encoding='utf-8')
        new = rewrite(text, rclasses)
        if new != text:
            path.write_text(new, encoding='utf-8')
            print('changed', path)
        for number, line in enumerate(new.splitlines(), 1):
            if any(re.search(r'\b' + slot + r'\b', line) for slot in OLD_SLOTS):
                print(f'  still names a removed slot: {path}:{number}')


if __name__ == '__main__':
    main(sys.argv[1:])
