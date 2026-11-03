#!/usr/bin/env python3
"""Step 14, run 14 of effect-slot-refactor.md: the Saga abilities (step 13 family 10: the chapter abilities and the
SagaWatch final-chapter abilities) lose their trigger-only wrappers.

- "This ability triggers only once each turn" (new OncePerTurnTriggerEffect(e)) on a final-chapter ability is
  TriggerLimit.ONCE_EACH_TURN (Tom Bombadil).
- A chapter ability's text is only its effect; its trigger condition is the lore-counter threshold (CR 714.2b), so an
  "if" in a chapter's text is a condition of the effect, checked on resolution, and every ConditionalEffect of the
  family stays an effect (The First Iroan Games, Summon: Fenrir, Heaven Sent, Inventive Iteration, Ifrit, …).
- A chapter ability with a trigger-only wrapper stops the script: a chapter takes no options.
- Kept: the delayed and registered abilities that chapters create (RegisterDelayedControllerSpellCastTriggerEffect,
  RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect's combat abilities), which are their own families' records;
  ChooseOneAtTriggerTimeEffect / ChooseOneAtRandomEffect (modal effects) and ChosenNameSpellCastTriggerEffect (a
  chapter effect, run 6c).

The peeling, re-indenting and import fixing are run 13's (scripts/step14-life-options.py), with this family's
factories. Running it again changes nothing.

Usage: python3 scripts/step14-saga-options.py [--dry-run] [--report]
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)


def load(name, file):
    spec = importlib.util.spec_from_file_location(name, os.path.join(HERE, file))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


life = load("life_options", "step14-life-options.py")


class Everything:
    """Every card: a chapter's ConditionalEffect is never an intervening "if" and stays an effect."""

    def __contains__(self, name):
        return True


life.FAMILY = re.compile(r"Triggers\.(chapter|finalChapterAbilityTriggers|finalChapterAbilityResolves)\(")
life.INTERVENING = set()
life.KEEP = Everything()
life.MANUAL = {}
life.tap.REMOVABLE = ["OncePerTurnTriggerEffect"]

_rewrite = life.rewrite


def placed(src, span, col):
    """src[span] re-indented so that its first character sits at column col. An effect that starts inside a line
    (under a peeled wrapper) has its continuation lines indented from that line's indent, so they keep it from the
    effect's new line: column(src, s) would cut them back to the margin."""
    s, e = span
    line_start = src.rfind("\n", 0, s) + 1
    line = src[line_start:s]
    start_col = len(line) - len(line.lstrip()) if line.strip() else life.column(src, s)
    shift = col - start_col
    lines = src[s:e].split("\n")
    out = [lines[0]]
    for text in lines[1:]:
        if shift >= 0:
            out.append(" " * shift + text if text.strip() else text)
        else:
            cut = min(-shift, len(text) - len(text.lstrip()))
            out.append(text[cut:])
    return "\n".join(out)


life.placed = placed


def rewrite(src, start, name, report):
    m = life.ABILITY.match(src, start)
    end = life.close_paren(src, m.end())
    args = life.split_args(src, m.end(), end - 1)
    if re.match(r"Triggers\.chapter\(", src[args[0][0]:args[0][1]]):
        for a, b in args[1:]:
            wrappers, _ = life.peel(src, (a, b), name)
            if wrappers:
                raise SystemExit("%s: a chapter ability takes no options: %s" % (name, src[start:end]))
        return None
    return _rewrite(src, start, name, report)


life.rewrite = rewrite

if __name__ == "__main__":
    life.main()
