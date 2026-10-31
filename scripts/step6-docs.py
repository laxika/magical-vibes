#!/usr/bin/env python3
"""EffectSlot refactor step 6: rewrites the static-ability authoring calls in agent-docs.

Usage: step6-docs.py FILE...

Code snippets move to the new API (``addStaticEffect``, ``getStaticEffects``, and
``List.of(StaticAbility.of(e), LegacySlots.triggered(slot, e))`` for the token and copy ability
lists), and prose that names ``EffectSlot.STATIC`` names the static ability instead. The tables'
``STATIC`` label is left alone; each doc that uses it gets a note that explains it.
"""
import pathlib
import re
import sys

NOTE = """> `STATIC` in this file means a static ability (CR 113.3d), written `addStaticEffect(e)`, or
> `target(f).addStaticEffect(e)` for an Aura's enchant ability. It functions from the zones its effect's
> rule names (`CardEffect.staticAbilityZones()`): the battlefield, the stack for kicker, buyback, gift,
> "can't be countered" and "costs less to cast", and the hand for splice. An ability that states its
> zones, such as Eminence, is written `addStaticEffect(AbilityZones.of(...), e)`. In a token's or a
> copy's ability list it is `StaticAbility.of(e)`, next to `LegacySlots.triggered(EffectSlot.X, e)` for
> a trigger. Readers use `getStaticEffects()`. There is no `EffectSlot.STATIC`.
"""
SPELL_NOTE_END = re.compile(r'^> There is no `EffectSlot\.SPELL`\.\n', re.M)
SLOT = r'(?:EffectSlot\.)?'
ENUM = pathlib.Path(__file__).resolve().parent.parent / (
    'magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/EffectSlot.java')
SLOT_NAMES = set(re.findall(r'^\s{4}([A-Z][A-Z0-9_]*)\b', ENUM.read_text(encoding='utf-8'), re.M)) | {'STATIC'}


def matching_paren(src, open_index):
    depth = 0
    for i in range(open_index, len(src)):
        if src[i] == '(':
            depth += 1
        elif src[i] == ')':
            depth -= 1
            if depth == 0:
                return i
    return -1


def split_top(text):
    parts, depth, start = [], 0, 0
    for i, c in enumerate(text):
        if c in '([{':
            depth += 1
        elif c in ')]}':
            depth -= 1
        elif c == ',' and depth == 0:
            parts.append(text[start:i].strip())
            start = i + 1
    parts.append(text[start:].strip())
    return parts


def convert_maps(src):
    pattern = re.compile(r'Map\.of\(\s*' + SLOT + r'(?=[A-Z_]+\s*,)')
    pos = 0
    while True:
        m = pattern.search(src, pos)
        if not m:
            return src
        open_index = src.index('(', m.start())
        close = matching_paren(src, open_index)
        if close < 0:
            return src
        parts = split_top(src[open_index + 1:close])
        if len(parts) % 2:
            pos = m.end()
            continue
        abilities = []
        for key, value in zip(parts[::2], parts[1::2]):
            slot = re.sub(r'^EffectSlot\.', '', key)
            if slot not in SLOT_NAMES:
                abilities = None
                break
            abilities.append(f'StaticAbility.of({value})' if slot == 'STATIC'
                             else f'LegacySlots.triggered(EffectSlot.{slot}, {value})')
        if abilities is None:
            pos = m.end()
            continue
        new = 'List.of(' + ', '.join(abilities) + ')'
        src = src[:m.start()] + new + src[close + 1:]
        pos = m.start() + len(new)


def convert(src):
    src = re.sub(r'\baddEffect\(\s*' + SLOT + r'STATIC\s*,\s*', 'addStaticEffect(', src)
    src = re.sub(r'\bgetEffects\(\s*EffectSlot\.STATIC\s*\)', 'getStaticEffects()', src)
    src = convert_maps(src)
    src = re.sub(r"own `EffectSlot\.STATIC` effects", "own static effects (`getStaticEffects()`)", src)
    src = re.sub(r"(?:the spell's )?own `EffectSlot\.STATIC`", "own static abilities", src)
    src = re.sub(r'\b(both|all) `EffectSlot\.STATIC`', r'\1 static abilities (`addStaticEffect`)', src)
    src = re.sub(r'\b(in|on|via|from|to) `EffectSlot\.STATIC`', r'\1 a static ability (`addStaticEffect`)', src)
    src = re.sub(r'`EffectSlot\.STATIC` (\+ )?', r'`addStaticEffect` \1', src)
    src = re.sub(r'`EffectSlot\.STATIC (\w+)`', r'`addStaticEffect(\1)`', src)
    src = src.replace('Effect (EffectSlot.STATIC)', 'Effect (`addStaticEffect`)')
    src = re.sub(r'(?<![\w`])EffectSlot\.STATIC(?![\w`])', 'static abilities', src)
    return src


def add_note(src):
    # Only the docs that use STATIC as a table or snippet label get the note.
    if NOTE in src or len(re.findall(r'(?<![\w.`])STATIC(?![\w`])', src)) < 10:
        return src
    m = SPELL_NOTE_END.search(src)
    if m:
        return src[:m.end()] + '\n' + NOTE + src[m.end():]
    m = re.search(r'^# .*\n', src, re.M)
    if m:
        return src[:m.end()] + '\n' + NOTE + src[m.end():]
    return NOTE + '\n' + src


for name in sys.argv[1:]:
    path = pathlib.Path(name)
    src = path.read_text(encoding='utf-8', errors='surrogateescape')
    new = add_note(convert(src))
    if new != src:
        path.write_text(new, encoding='utf-8', errors='surrogateescape')
        print(path)
