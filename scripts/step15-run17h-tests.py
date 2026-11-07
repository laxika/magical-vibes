#!/usr/bin/env python3
"""Step 15, run 17h: the spell-cast collector tests of an authored `SpellCastTriggerEffect` on the "a player casts",
"you cast" and exile watches collect it through `collectCastAbility`.

No card authors the record on those watches any more (emblems and grants hold `TriggeredAbility`s), so their registry
handlers are gone; the tests judge the record as the collector does (`CastAbility.of(record, null)`). Each
`registry.dispatch(match, watch, effect, ctx)` of a record variable becomes
`sut.collectCastAbility(match, CastAbility.of(effect, null), ctx, perMana)`, `perMana` true for the "you cast" watch,
whose handler counted the mana a source produced. Idempotent.

Usage: step15-run17h-tests.py <test file>
"""
import pathlib
import re
import sys

WATCHES = {
    "SpellAbilityWatch.ON_ANY_PLAYER_CASTS_SPELL": "false",
    "SpellAbilityWatch.ON_CONTROLLER_CASTS_SPELL": "true",
    "SpellAbilityWatch.EXILE_ON_CONTROLLER_CASTS_SPELL": "false",
}
RECORD_VARIABLE = re.compile(r"var (\w+) = (?:new SpellCastTriggerEffect\(|SpellCastTriggerEffect\.\w+\()")


def split_arguments(text):
    """The top-level comma-separated arguments of a call's argument text."""
    arguments, depth, current = [], 0, []
    for char in text:
        if char in "([{":
            depth += 1
        elif char in ")]}":
            depth -= 1
        if char == "," and depth == 0:
            arguments.append("".join(current).strip())
            current = []
        else:
            current.append(char)
    arguments.append("".join(current).strip())
    return arguments


def convert_block(block):
    records = set(RECORD_VARIABLE.findall(block))
    if not records:
        return block
    out, index = [], 0
    while True:
        start = block.find("registry.dispatch(", index)
        if start < 0:
            out.append(block[index:])
            return "".join(out)
        open_paren = start + len("registry.dispatch")
        depth, end = 0, open_paren
        for end in range(open_paren, len(block)):
            if block[end] == "(":
                depth += 1
            elif block[end] == ")":
                depth -= 1
                if depth == 0:
                    break
        arguments = split_arguments(block[open_paren + 1:end])
        if len(arguments) == 4 and arguments[1] in WATCHES and arguments[2] in records:
            match, watch, effect, context = arguments
            out.append(block[index:start])
            line_start = block.rfind("\n", 0, start) + 1
            indent = len(block[line_start:]) - len(block[line_start:].lstrip())
            call = f"sut.collectCastAbility({match}, CastAbility.of({effect}, null), {context}, {WATCHES[watch]})"
            if start - line_start + len(call) + 1 > 120:
                call = (f"sut.collectCastAbility({match}, CastAbility.of({effect}, null),\n"
                        f"{' ' * (indent + 8)}{context}, {WATCHES[watch]})")
            out.append(call)
        else:
            out.append(block[index:end + 1])
        index = end + 1


def main(path):
    file = pathlib.Path(path)
    source = file.read_text()
    blocks = re.split(r"(?=\n    (?:    )?@Test\n)", source)
    converted = "".join(convert_block(block) for block in blocks)
    if converted != source:
        file.write_text(converted)
        print(f"converted {path}: {source.count('registry.dispatch(') - converted.count('registry.dispatch(')} calls")
    else:
        print(f"{path}: nothing to convert")


if __name__ == "__main__":
    main(sys.argv[1])
