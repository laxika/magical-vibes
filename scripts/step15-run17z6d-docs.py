#!/usr/bin/env python3
"""Step 15, run 17z6d: agent-docs for the colors spent read from the triggering spell and the non-targeting counters.

"For each color of mana spent to cast that spell" (`ColorsSpentToCast`, Magmablood Archaic) reads the colors the spell
recorded as it was cast (`StackEntry.colorsSpentToCast`) as the ability resolves, where the cast collector gave the
ability the colors spent as its X; `CounterSpellingEffect.targetsTriggeringSpell()` says whether a cast trigger with the
effect targets the triggering spell. Fragment edits in `ARCHITECTURE.md`, `EFFECTS_QUICK_REFERENCE.md`,
`EFFECTS_INDEX.md`, `ORACLE_TEXT_EFFECT_MAP.md` and `CARD_PATTERNS_CREATURES_TRIGGERED.md`. Idempotent. Fails if a
doc or skill still says the cast collector gives the ability the colors spent as its X.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude" / "skills"
REMOVED = "colors spent on the spell as its X"
PROPERTY = ("`targetsTriggeringSpell()` (default `true`) says whether a cast trigger with it goes on the stack "
            "targeting the triggering spell; it is `false` for Counterbalance's, Swindler's Scheme's and Mirrored "
            "Depths' effects, which still resolve after the spell was countered in response (CR 608.2b).")

EDITS = {
    "ARCHITECTURE.md": [
        ("reads the spell's mana value as the ability's X the same way, and \"for each color of mana spent to cast that "
         "spell\" (`ColorsSpentToCast`) the colors spent on it.",
         "reads the spell's mana value as the ability's X the same way. \"For each color of mana spent to cast that "
         "spell\" (`ColorsSpentToCast`) reads the colors the spell recorded as it was cast (`StackEntry."
         "colorsSpentToCast`) as the ability resolves, from the spell on the stack, else from the entry's snapshot of "
         "it (CR 608.2h)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("On a cast trigger it is the colors spent on the triggering spell, which the cast collector gives the ability "
         "as its X (Magmablood Archaic's",
         "On a cast trigger it is the colors spent on the triggering spell, which the spell records as it is cast "
         "(`StackEntry.colorsSpentToCast`) and the ability reads as it resolves, as the spell last existed on the "
         "stack once it has left (Magmablood Archaic's"),
        ("- `CounterSpellingEffect` — marker for \"counter target spell\". Impl `CounterSpellEffect`,\n",
         "- `CounterSpellingEffect` — marker for \"counter target spell\". " + PROPERTY
         + " Impl `CounterSpellEffect`,\n"),
    ],
    "EFFECTS_INDEX.md": [
        ("Wrap the spell-cast trigger in `MayEffect` for Counterbalance's optional reveal.",
         "Wrap the spell-cast trigger in `MayEffect` for Counterbalance's optional reveal. Its "
         "`targetsTriggeringSpell()` is `false`: the ability doesn't target the spell, so it still resolves after the "
         "spell was countered in response (CR 608.2b)."),
        ("The revealed card stays on top when declined or when the card types do not match.",
         "The revealed card stays on top when declined or when the card types do not match. Its "
         "`targetsTriggeringSpell()` is `false`, as Counterbalance's is."),
        ("flips for the caster of the spell that caused the trigger and counters that spell if the caster loses; "
         "targets the triggering spell and emits",
         "flips for the caster of the spell that caused the trigger and counters that spell if the caster loses; "
         "doesn't target the triggering spell (`targetsTriggeringSpell()` is `false`), so the caster still flips once "
         "it has left the stack, and emits"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("Magmablood Archaic; the cast collector gives the ability the colors spent on the spell as its X, which "
         "`ColorsSpentToCast` reads |",
         "Magmablood Archaic; `ColorsSpentToCast` reads the colors the triggering spell recorded as it was cast, as "
         "the ability resolves (as it last existed on the stack if it was countered) |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`BoostAllOwnCreaturesEffect(new ColorsSpentToCast(), new Fixed(0))`; the cast collector gives the ability "
         "the spell's colors spent as its X |",
         "`BoostAllOwnCreaturesEffect(new ColorsSpentToCast(), new Fixed(0))`; `ColorsSpentToCast` reads the colors "
         "the spell recorded as it was cast, as the ability resolves |"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                sys.exit(f"{name}: fragment not found: {old[:80]}")
            text = text.replace(old, new, 1)
        path.write_text(text)
    stale = []
    for path in list(DOCS.glob("*.md")) + list(SKILLS.glob("**/*.md")):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if REMOVED in line or "spell's colors spent as its X" in line:
                stale.append(f"{path.relative_to(ROOT)}:{number}")
    if stale:
        sys.exit("still names " + REMOVED + ": " + ", ".join(stale))
    print("docs updated")


if __name__ == "__main__":
    main()
