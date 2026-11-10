#!/usr/bin/env python3
"""Step 15, run 17z13e5: agent-docs follow the enter defaults.

- Portcullis's "if there are two or more other creatures on the battlefield" is its intervening "if"
  (`AnotherCreatureOnBattlefield(2)`); `ExileTriggeringCreatureUntilSourceLeavesEffect` has no count
  (`ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_QUICK_REFERENCE.md`, `EFFECTS_INDEX.md`).
- `AnotherCreatureOnBattlefield` takes a count (`EFFECTS_INDEX.md`, where its entry was listed twice).
- The "you may" rows no longer name the deleted `EnterTriggerCollectorService.handleEnterMay`
  (`ORACLE_TEXT_EFFECT_MAP.md`, `CARD_PATTERNS_CREATURES_TRIGGERED.md`).

Fails if a doc still names a deleted enter handler or the effect's count. Idempotent.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

OLD_ANOTHER = ('`AnotherCreatureOnBattlefield()` (a creature other than the triggering permanent; as a death triggers the '
               'ability the creatures that died with it count, CR 603.10a: Lifeline\'s intervening "if another creature '
               'is on the battlefield"),')
NEW_ANOTHER = ('`AnotherCreatureOnBattlefield([count])` (at least `count`, default 1, creatures other than the triggering '
               'permanent; as a death triggers the ability the creatures that died with it count, CR 603.10a: '
               'Lifeline\'s intervening "if another creature is on the battlefield", Portcullis\'s "if there are two or '
               'more other creatures on the battlefield" `(2)`),')

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `ExileTriggeringCreatureUntilSourceLeavesEffect(2)` in `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY)` "
         "| TRIGGER | Portcullis. The collector binds the entering permanent by identity and rechecks the other-creature "
         "condition when the trigger resolves; the slot includes the source's own entry.",
         "| `ExileTriggeringCreatureUntilSourceLeavesEffect()` in `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANY)` "
         "with `TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield(2))` | TRIGGER | Portcullis. "
         "The entry records the entering permanent; the intervening \"if\" is checked as it enters and again as the "
         "ability resolves (CR 603.4); the trigger includes the source's own entry."),
        ("; `MayEffect` is queued as a may-ability by `EnterTriggerCollectorService.handleEnterMay` |",
         "; the `MayEffect` goes on the stack through the watch's default, asked as it resolves (CR 603.5) |"),
        ("`handleEnterMay` binds the source permanent even for ally scans (no default target player), so the wrapped "
         "`PutCountersOnSourceEffect` resolves against the source.",
         "The ability's entry has the source permanent, so the wrapped `PutCountersOnSourceEffect` resolves against the "
         "source."),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("handleEnterMay binds the source permanent for ally scans, so the may-ability resolves PutCountersOnSourceEffect "
         "against Godtracker itself",
         "the ability's entry has the source permanent, so the may-ability resolves PutCountersOnSourceEffect against "
         "Godtracker itself"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ExileTriggeringCreatureUntilSourceLeavesEffect(int minimumOtherCreatures)` — non-targeting global "
         "creature-entry trigger: binds the entering permanent by identity, checks the other-creature count when the "
         "trigger is created and resolves,",
         "- `ExileTriggeringCreatureUntilSourceLeavesEffect()` — non-targeting global creature-entry trigger: exiles the "
         "entering permanent the entry records (nothing once it has left),"),
        ("Portcullis = `(2)`",
         "Portcullis's \"if there are two or more other creatures on the battlefield\" is its intervening \"if\", "
         "`TriggerOptions.DEFAULT.withInterveningIf(new AnotherCreatureOnBattlefield(2))`"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ExileTriggeringCreatureUntilSourceLeavesEffect` | `()` or `(int minimumOtherCreatures)` | non-targeting global "
         "creature-entry trigger: binds the entering permanent by identity, checks the other-creature count when the "
         "trigger is created and resolves,",
         "| `ExileTriggeringCreatureUntilSourceLeavesEffect` | `()` | non-targeting global creature-entry trigger: exiles "
         "the entering permanent the entry records (Portcullis's \"two or more other creatures\" is its intervening "
         "\"if\", `AnotherCreatureOnBattlefield(2)`),"),
        (OLD_ANOTHER + " " + OLD_ANOTHER, NEW_ANOTHER),
    ],
}
FORBIDDEN = re.compile(r"handleEnterMay|handleAnyCreatureEnterMay|handleTokenEnterMay|handleAllyCreatureEnterModal|"
                       r"handleCreatureEnterReturnToHand|handleCreatureEnterExileGraveyardCard|"
                       r"handleAnyCreatureExileUntilSourceLeaves|minimumOtherCreatures|UntilSourceLeavesEffect\(2\)")


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{name}: pattern not found: {old[:80]!r}")
            text = text.replace(old, new, 1)
        path.write_text(text)
    stale = [f"{p.name}:{text.count(chr(10), 0, m.start()) + 1}" for p in DOCS.glob("*.md")
             for text in [p.read_text()] for m in FORBIDDEN.finditer(text)]
    if stale:
        sys.exit(f"stale: {stale}")


if __name__ == "__main__":
    main()
