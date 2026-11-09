#!/usr/bin/env python3
"""Step 15, run 17z6c: agent-docs for Foul Emissary's "when you sacrifice this while casting a spell with emerge".

`CreateTokenForEmergeSacrificeEffect` is gone: the trigger names the spell being cast,
`Triggers.sacrificed(THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE))`, judged on the zone change's
`castingSpell` (the spell its controller was casting as it left: a sacrifice for that spell's costs or for a mana
ability activated with a cast payment intent). Line edits in `ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_QUICK_REFERENCE.md`,
`EFFECTS_INDEX.md` and `ARCHITECTURE.md`. Idempotent. Fails if a doc or skill still names the removed record outside a
"Gone" or "removed" line.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude" / "skills"
REMOVED = "CreateTokenForEmergeSacrificeEffect"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("- \"When you sacrifice this creature while casting a spell with emerge, create a 3/2 colorless Eldrazi Horror "
         "creature token.\" -> `Triggers.sacrificed(THIS)` + `CreateTokenForEmergeSacrificeEffect(new "
         "CreateTokenEffect(...))`. The sacrifice-only trigger checks the casting spell's emerge capability, including "
         "a normal cast of an emerge card; it does not require the emerge alternate cost to be chosen (Foul Emissary).",
         "- \"When you sacrifice this creature while casting a spell with emerge, create a 3/2 colorless Eldrazi Horror "
         "creature token.\" -> `Triggers.sacrificed(THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE))` + "
         "`new CreateTokenEffect(...)`. The trigger reads the spell its controller was casting as it left "
         "(`ZoneChangeEvent.castingSpell`): sacrificed for any of that spell's costs, its emerge cost or not, or to a "
         "mana ability activated to pay for it (Ashnod's Altar, with a cast `ManaPaymentIntent`), as the card's ruling "
         "says (Foul Emissary)."),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `CreateTokenForEmergeSacrificeEffect(CreateTokenEffect tokenEffect)` — `Triggers.sacrificed(THIS)` "
         "sacrifice-only descriptor for \"when you sacrifice this creature while casting a spell with emerge, create a "
         "token\"; checks the casting spell's emerge capability and queues the token trigger above the spell after "
         "casting costs finish. Foul Emissary",
         "- `CreateTokenForEmergeSacrificeEffect` — **Gone** (effect-slot-refactor.md step 15, run 17z6c): \"when you "
         "sacrifice this creature while casting a spell with emerge\" is "
         "`Triggers.sacrificed(THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE))` with the token creation "
         "as the ability's effect (Foul Emissary)."),
    ],
    "EFFECTS_INDEX.md": [
        ("| `CreateTokenForEmergeSacrificeEffect` | `(CreateTokenEffect tokenEffect)` | `Triggers.sacrificed(THIS)` "
         "sacrifice-only trigger descriptor: when the source was sacrificed as part of casting a spell with emerge, "
         "queues the wrapped token creation above that spell after casting costs finish. Foul Emissary |",
         "| ~~`CreateTokenForEmergeSacrificeEffect`~~ (removed) | | Use "
         "`Triggers.sacrificed(THIS).whileCasting(new CardKeywordPredicate(Keyword.EMERGE))` with the token creation "
         "as the ability's effect (Foul Emissary) |"),
    ],
    "ARCHITECTURE.md": [
        ("`onlyDuringYourTurn()` → the watch without it)",
         "`onlyDuringYourTurn()` → the watch without it; `whileCasting(CardPredicate)`, \"while casting a spell with "
         "emerge\", is judged by `TriggerMatcher` on `ZoneChangeEvent.castingSpell`, the spell `RulesEventLog."
         "setCastingSpell` names while its costs, or a mana ability activated with a cast `ManaPaymentIntent`, are "
         "paid)"),
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
            if REMOVED in line and "**Gone**" not in line and "(removed)" not in line:
                stale.append(f"{path.relative_to(ROOT)}:{number}")
    if stale:
        sys.exit("still names " + REMOVED + ": " + ", ".join(stale))
    print("docs updated")


if __name__ == "__main__":
    main()
