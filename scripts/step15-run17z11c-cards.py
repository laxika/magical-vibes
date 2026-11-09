#!/usr/bin/env python3
"""Step 15, run 17z11c: the block and "becomes blocked by" gates are trigger filters on the cards.

Idempotent; run per letter module: `step15-run17z11c-cards.py <letter> [repo-root]` (or `all`).

- `DestroyCombatOpponentAtEndOfCombatEffect(filter, ...)`: the filter on "that creature" moves to the trigger it is
  written on (`Triggers.blocks().withFilter(...)`, `Triggers.becomesBlocked().withFilter(...)`); "a creature" needs
  none.
- Corrosive Ooze: "an equipped creature" is `withFilter(new PermanentIsEquippedPredicate())`.
- `BoostSelfWhenBlockingKeywordEffect(K, p, t)`: `withFilter(new PermanentHasKeywordPredicate(K))` and
  `new BoostSelfEffect(p, t)`.
- `BoostSelfWhenCombatOpponentMatchesEffect(F, p, t[, keywords])`: `withFilter(F)` and `new BoostSelfEffect(p, t)`
  (left out for +0/+0) plus `new GrantKeywordEffect(keywords, GrantScope.SELF)`.
- Shield of the Righteous' comment no longer names the removed interface.
"""
import pathlib
import re
import sys

letter = sys.argv[1] if len(sys.argv) > 1 else 'all'
root = pathlib.Path(sys.argv[2] if len(sys.argv) > 2 else '.')
base = root / 'magical-vibes-card'
letters = [p.name for p in sorted(base.iterdir()) if len(p.name) == 1 or p.name == 'shared'] if letter == 'all' else [letter]

PKG_EFFECT = 'com.github.laxika.magicalvibes.model.effect.'
PKG_FILTER = 'com.github.laxika.magicalvibes.model.filter.'
ANY_CREATURE = 'new PermanentIsCreaturePredicate()'


def split_args(text, start):
    """Args of the call whose '(' is at text[start]; returns (args, end index after ')')."""
    depth, args, cur, i = 0, [], '', start
    while True:
        c = text[i]
        if c in '([{':
            depth += 1
            if depth > 1:
                cur += c
        elif c in ')]}':
            depth -= 1
            if depth == 0:
                args.append(cur.strip())
                return [a for a in args if a], i + 1
            cur += c
        elif c == ',' and depth == 1:
            args.append(cur.strip())
            cur = ''
        else:
            cur += c
        i += 1


def ensure_import(text, fqcn):
    if f'import {fqcn};' in text:
        return text
    simple = fqcn.rsplit('.', 1)[1]
    if re.search(rf'import [\w.]+\.{simple};', text):
        return text
    imports = [m for m in re.finditer(r'^import ([\w.]+);\n', text, re.M) if not m.group(1).startswith('java.')]
    after = [m for m in imports if m.group(1) > fqcn]
    if after:
        pos = after[0].start()
    elif imports:
        pos = imports[-1].end()
    else:
        pos = text.index('\n', text.index('package')) + 1
    return text[:pos] + f'import {fqcn};\n' + text[pos:]


def drop_unused_import(text, simple):
    body = re.sub(r'^import [^\n]+;\n', '', text, flags=re.M)
    if re.search(rf'\b{simple}\b', body):
        return text
    return re.sub(rf'^import [\w.]+\.{simple};\n', '', text, flags=re.M)


def resolve_var(text, expr):
    """The expression a local predicate variable was initialised with, else the expression itself."""
    if re.fullmatch(r'\w+', expr):
        m = re.search(rf'\b\w+(?:<[^>]*>)?\s+{expr}\s*=\s*', text)
        if m:
            j = m.end()
            depth = 0
            k = j
            while True:
                c = text[k]
                if c in '([{':
                    depth += 1
                elif c in ')]}':
                    depth -= 1
                elif c == ';' and depth == 0:
                    break
                k += 1
            return ' '.join(text[j:k].split())
    return expr


def add_filter_to_triggers(text, effect_start, effect_end, var_name, filter_expr):
    """Adds .withFilter(filter_expr) to the trigger of the addTrigger call holding the effect (inline) or to every
    addTrigger call that passes var_name."""
    def patch_call(t, call_start):
        m = re.compile(r'Triggers\.(blocks|becomesBlocked)\(\)').search(t, call_start)
        if t[m.end():m.end() + len('.withFilter(')] == '.withFilter(':
            return t
        return t[:m.end()] + f'.withFilter({filter_expr})' + t[m.end():]
    if var_name is None:
        call_start = text.rfind('addTrigger(', 0, effect_start)
        return patch_call(text, call_start)
    for m in reversed(list(re.finditer(rf'addTrigger\(Triggers\.(?:blocks|becomesBlocked)\(\)[^;]*?\b{var_name}\)', text))):
        text = patch_call(text, m.start())
    return text


def convert_destroy(text):
    cls = 'DestroyCombatOpponentAtEndOfCombatEffect'
    while True:
        found = None
        for m in re.finditer(rf'new {cls}\(', text):
            args, end = split_args(text, m.end() - 1)
            if len(args) >= 2 and args[0] not in ('true', 'false'):
                found = (m, args, end)
                break
        if not found:
            return text
        m, args, end = found
        filter_expr = resolve_var(text, args[0])
        stmt_start = text.rfind(';', 0, m.start()) + 1
        assign = re.search(rf'{cls}\s+(\w+)\s*=\s*$', text[stmt_start:m.start()].replace('\n', ' '))
        var_name = assign.group(1) if assign else None
        text = text[:m.start()] + f'new {cls}(' + ', '.join(args[1:]) + ')' + text[end:]
        if filter_expr != ANY_CREATURE:
            text = add_filter_to_triggers(text, m.start(), m.start(), var_name, args[0])
        if re.fullmatch(r'\w+', args[0]) and not re.search(rf'\b{args[0]}\b(?!\s*=)', text):
            text = re.sub(rf'\n[ \t]*[\w<>]+\s+{args[0]}\s*=[^;]*;\n', '\n', text)
        text = drop_unused_import(text, 'PermanentIsCreaturePredicate')


def convert_boost(text):
    changed = True
    while changed:
        changed = False
        for cls in ('BoostSelfWhenBlockingKeywordEffect', 'BoostSelfWhenCombatOpponentMatchesEffect'):
            m = re.search(rf'new {cls}\(', text)
            if not m:
                continue
            args, end = split_args(text, m.end() - 1)
            imports = []
            if cls == 'BoostSelfWhenBlockingKeywordEffect':
                filter_expr = f'new PermanentHasKeywordPredicate({args[0]})'
                imports.append(PKG_FILTER + 'PermanentHasKeywordPredicate')
                p, t, kws = args[1], args[2], None
            else:
                filter_expr = args[0]
                p, t = args[1], args[2]
                kws = args[3] if len(args) > 3 else None
            effects = []
            if (p, t) != ('0', '0'):
                effects.append(f'new BoostSelfEffect({p}, {t})')
                imports.append(PKG_EFFECT + 'BoostSelfEffect')
            if kws:
                single = re.fullmatch(r'Set\.of\(([\w.]+)\)', kws)
                effects.append(f'new GrantKeywordEffect({single.group(1) if single else kws}, GrantScope.SELF)')
                imports += [PKG_EFFECT + 'GrantKeywordEffect', PKG_EFFECT + 'GrantScope']
            text = text[:m.start()] + ', '.join(effects) + text[end:]
            text = add_filter_to_triggers(text, m.start(), m.start(), None, filter_expr)
            for fqcn in imports:
                text = ensure_import(text, fqcn)
            text = re.sub(rf'^import [\w.]+\.{cls};\n', '', text, flags=re.M)
            text = drop_unused_import(text, 'Set')
            changed = True
    return text


def convert(path):
    text = path.read_text()
    new = text
    if 'DestroyCombatOpponentAtEndOfCombatEffect' in new:
        new = convert_destroy(new)
    if 'new DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect()' in new and 'PermanentIsEquippedPredicate' not in new:
        new = new.replace('Triggers.blocks(), new DestroyEquipmentOnEquipped',
                          'Triggers.blocks().withFilter(equipped),\n                new DestroyEquipmentOnEquipped')
        new = re.sub(r'Triggers\.becomesBlocked\(\)(?=,\s*TriggerOptions\.EACH_BLOCKER,\s*new DestroyEquipmentOnEquipped)',
                     'Triggers.becomesBlocked().withFilter(equipped)', new)
        new = new.replace('        addTrigger(Triggers.blocks().withFilter(equipped)',
                          '        PermanentIsEquippedPredicate equipped = new PermanentIsEquippedPredicate();\n'
                          '        addTrigger(Triggers.blocks().withFilter(equipped)', 1)
        new = ensure_import(new, PKG_FILTER + 'PermanentIsEquippedPredicate')
    if 'BoostSelfWhenBlockingKeywordEffect' in new or 'BoostSelfWhenCombatOpponentMatchesEffect' in new:
        new = convert_boost(new)
    if path.name == 'ShieldOfTheRighteous.java':
        new = new.replace(
            "        // controller's next untap step. SkipNextUntapEffect is a CombatOpponentReferencingEffect, so\n"
            "        // the trigger auto-targets the blocked attacker.\n",
            "        // controller's next untap step. The card declares no target, so \"that creature\" is the blocked\n"
            "        // attacker the stack entry records.\n")
    if new != text:
        new = re.sub(r'\n{3,}', '\n\n', new)
        path.write_text(new)
        return True
    return False


count = 0
for L in letters:
    for path in sorted((base / L / 'src/main/java').rglob('*.java')):
        if convert(path):
            count += 1
            print('converted', path.name)
print('cards converted:', count)
