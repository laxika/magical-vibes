#!/usr/bin/env python3
"""Step 15, run 17z6f: agent-docs for the Kirins' "with that spell's mana value" read from the triggering spell.

`effectFilterReadsManaValueX` is gone: Celestial Kirin's destroy and Infernal Kirin's discard filter by the new
`PermanentManaValueEqualsTriggeringSpellPredicate` / `CardManaValueEqualsTriggeringSpellPredicate`, bound to the entry's
triggering spell as the ability resolves (`AmountEvaluationService.bindTriggeringSpellManaValue`). The target
restrictions by the spell's mana value (Skyfire Kirin, Hammerhead Tyrant, Krile Baldesion) keep the ability's or the
choice's X. Fragment edits; the files keep their own encoding. Fails if a doc or skill still says an effect's own
filter reads the spell's mana value as the ability's X.

Idempotent.

Usage: step15-run17z6f-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`DestroyAllPermanentsEffect(new PermanentManaValueEqualsXPredicate())` | `Triggers.youCastSpell().withFilter(new "
         "CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new CardSubtypePredicate(ARCANE))))` | Celestial "
         "Kirin. An effect filter that reads X on a cast trigger gets the spell's mana value (with its chosen X) as the "
         "ability's X |",
         "`DestroyAllPermanentsEffect(new PermanentManaValueEqualsTriggeringSpellPredicate())` | "
         "`Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new "
         "CardSubtypePredicate(ARCANE))))` | Celestial Kirin. The predicate reads the triggering spell's mana value (with "
         "its chosen X) as the ability resolves, from the entry's snapshot once the spell has left the stack |"),
        ("`RevealHandAndDiscardMatchingCardsEffect(new CardManaValueEqualsXPredicate())` | `Triggers.youCastSpell()"
         ".withFilter(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new CardSubtypePredicate(ARCANE))))` "
         "| Infernal Kirin. The target choice and the entry get the spell's mana value as the ability's X |",
         "`RevealHandAndDiscardMatchingCardsEffect(new CardManaValueEqualsTriggeringSpellPredicate())` | "
         "`Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(new CardSubtypePredicate(SPIRIT), new "
         "CardSubtypePredicate(ARCANE))))` | Infernal Kirin. The predicate reads the triggering spell's mana value as the "
         "ability resolves (the target choice's entry records the spell) |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`RevealHandAndDiscardMatchingCardsEffect(new CardManaValueEqualsXPredicate())`; the spell's mana value is the "
         "ability's X |",
         "`RevealHandAndDiscardMatchingCardsEffect(new CardManaValueEqualsTriggeringSpellPredicate())`; the triggering "
         "spell's mana value is read as the ability resolves |"),
        ("`DestroyAllPermanentsEffect(new PermanentManaValueEqualsXPredicate())`; the spell's mana value is the ability's "
         "X |",
         "`DestroyAllPermanentsEffect(new PermanentManaValueEqualsTriggeringSpellPredicate())`; the triggering spell's "
         "mana value is read as the ability resolves |"),
    ],
    "ARCHITECTURE.md": [
        ("An effect's own filter \"with that spell's mana value\" (`DestroyAllPermanentsEffect` with "
         "`PermanentManaValueEqualsXPredicate`, `RevealHandAndDiscardMatchingCardsEffect` with "
         "`CardManaValueEqualsXPredicate`: the Kirins) reads the spell's mana value as the ability's X the same way.",
         "An effect's own filter \"with that spell's mana value\" (`DestroyAllPermanentsEffect` with "
         "`PermanentManaValueEqualsTriggeringSpellPredicate`, `RevealHandAndDiscardMatchingCardsEffect` with "
         "`CardManaValueEqualsTriggeringSpellPredicate`: the Kirins) is bound by its handler to the entry's triggering "
         "spell as the ability resolves (`AmountEvaluationService.bindTriggeringSpellManaValue`: the live spell, else the "
         "entry's snapshot, with its chosen X); only the target restrictions by it keep the spell's mana value as the "
         "ability's or the choice's X."),
    ],
    "PREDICATES_REFERENCE.md": [
        ("| `PermanentManaValueEqualsTriggeringPermanentPredicate` | `()` | permanents whose mana value equals the mana value "
         "of the permanent whose entry caused the trigger | `gameData` + `triggeringPermanentId` |\n",
         "| `PermanentManaValueEqualsTriggeringPermanentPredicate` | `()` | permanents whose mana value equals the mana value "
         "of the permanent whose entry caused the trigger | `gameData` + `triggeringPermanentId` |\n"
         "| `PermanentManaValueEqualsTriggeringSpellPredicate` | `()` | permanents with the mana value of the spell that "
         "triggered the resolving ability (\"destroy all permanents with that spell's mana value\", Celestial Kirin), read "
         "as it resolves: the live spell, else the entry's snapshot, its chosen X counted. Bound by "
         "`AmountEvaluationService.bindTriggeringSpellManaValue` in `DestroyAllPermanentsEffect`'s handler; matches nothing "
         "elsewhere | the resolving entry |\n"),
        ("| `CardSharesNameWithTriggeringSpellPredicate` | `()` |",
         "| `CardManaValueEqualsTriggeringSpellPredicate` | `()` | a card with the mana value of the spell that triggered "
         "the resolving ability (\"discards all cards with that spell's mana value\", Infernal Kirin), read as it resolves "
         "as `PermanentManaValueEqualsTriggeringSpellPredicate` is. Bound in `RevealHandAndDiscardMatchingCardsEffect`'s "
         "handler; matches nothing elsewhere |\n"
         "| `CardSharesNameWithTriggeringSpellPredicate` | `()` |"),
    ],
}

STALE = [
    "effectFilterReadsManaValueX",
    "An effect filter that reads X on a cast trigger gets the spell's mana value",
    "reads the spell's mana value as the ability's X the same way",
]


def read(path):
    return path.read_bytes().decode("utf-8", errors="surrogateescape")


def write(path, text):
    path.write_bytes(text.encode("utf-8", errors="surrogateescape"))


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = read(path)
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                raise SystemExit(f"{name}: fragment missing: {old[:80]}")
            text = text.replace(old, new, 1)
            changed = True
        if changed:
            write(path, text)
            print(f"{name}: updated")
    stale = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = read(path)
        stale += [f"{path.name}: {phrase}" for phrase in STALE if phrase in text]
    if stale:
        raise SystemExit("stale mentions:\n" + "\n".join(stale))


if __name__ == "__main__":
    main()
