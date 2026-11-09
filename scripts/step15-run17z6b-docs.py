#!/usr/bin/env python3
"""Step 15, run 17z6b: agent-docs for the cast triggers' "that spell's mana value" read as the ability resolves.

`TriggeringSpellManaValueEffect` is gone: the cast collector records the cast event's value on every entry (the kick
count, else the spell's mana value as it triggered), and "that spell's mana value" is `TargetSpellManaValue`. Fragment
edits in `ORACLE_TEXT_EFFECT_MAP.md`, `EFFECTS_QUICK_REFERENCE.md`, `EFFECTS_INDEX.md`, `ARCHITECTURE.md` and
`CARD_PATTERNS_CREATURES_TRIGGERED.md`. Idempotent. Fails if a doc or skill still names the removed interface.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`new DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(new EventValue())`",
         "`new DealDamageToRandomOpponentOrTheirPlaneswalkerEffect(new TargetSpellManaValue())`"),
        ("the opponent is selected randomly at resolution and the player/planeswalker choice is non-targeting |",
         "the opponent is selected randomly at resolution and the player/planeswalker choice is non-targeting; the "
         "damage is the spell's mana value as the ability resolves, as it last existed on the stack if it was "
         "countered |"),
        ("`CounterTriggeringSpellAndCreateTokensEffect(CreateTokenEffect(EventValue, Bird 1/1 white-blue flying))`",
         "`CounterTriggeringSpellAndCreateTokensEffect(CreateTokenEffect(TargetSpellManaValue, Bird 1/1 white-blue "
         "flying))`"),
        ("The trigger carries the caster and snapshots the spell's mana value independently, so Birds are created even "
         "when the spell cannot be countered or has already left the stack.",
         "The trigger carries the caster, and `TargetSpellManaValue` reads the spell as it last existed on the stack, "
         "so Birds are created even when the spell cannot be countered or has already left the stack."),
        ("`SpellCastTriggerEffect(new CardTypePredicate(CREATURE), List.of(new DestroyAllPermanentsEffect(new "
         "PermanentHasSubtypePredicate(REFLECTION)), new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect("
         "\"Reflection\", new EventValue(), new EventValue(), WHITE, List.of(REFLECTION), Set.of(), Set.of())))`",
         "`.withFilter(new CardTypePredicate(CREATURE))` on the cast trigger with `new DestroyAllPermanentsEffect(new "
         "PermanentHasSubtypePredicate(REFLECTION))`, `new CreateTokenForTriggeringPlayerEffect(new CreateTokenEffect("
         "\"Reflection\", new TargetSpellManaValue(), new TargetSpellManaValue(), WHITE, List.of(REFLECTION), Set.of(), "
         "Set.of()))`"),
        ("The trigger carries the caster without targeting them and snapshots the spell's full mana value for both "
         "token dimensions",
         "The trigger carries the caster without targeting them; both token dimensions read the spell's mana value "
         "(with X) as the ability resolves"),
        ("`CascadeEffect` implements `TriggeringSpellManaValueEffect`, so the generic spell-cast collector sets the "
         "stack entry's event value to the triggering spell's announced mana value for `CascadeEffectHandler` "
         "(including an announced X value)",
         "`CascadeEffectHandler` compares with the triggering spell's mana value (with its announced X) as the "
         "ability resolves (`TargetSpellManaValue`, as it last existed on the stack if it was countered)"),
        ("with the triggering spell's mana value (the entry's event value) as its X;",
         "with the triggering spell's mana value (the cast event's value the entry records as it triggers) as its X;"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("the reflexive graveyard target choice's X is the entry's event value, the triggering spell's mana value "
         "(Saruman of Many Colors)",
         "the reflexive graveyard target choice's X is the entry's event value, the triggering spell's mana value the "
         "cast collector records on every entry (Saruman of Many Colors)"),
        ("accepting resolves the wrapped effect. Implements `TriggeringSpellManaValueEffect` so `EventValue()` can "
         "carry a spell-cast mana value through the trigger.",
         "accepting resolves the wrapped effect. On a cast trigger, \"life equal to that spell's mana value\" is "
         "`new TargetSpellManaValue()` (G'raha Tia, Scion Reborn)."),
        ("pair with `Triggers.youCastSpell().nthThisTurn(1)` and `EventValue` for Vial Smasher the Fierce",
         "pair with `Triggers.youCastSpell().nthThisTurn(1)` and `TargetSpellManaValue` for Vial Smasher the Fierce"),
        ("Spell-cast triggers also snapshot the triggering spell's full mana value for `EventValue` power/toughness.",
         "On a spell-cast trigger, \"X is that spell's mana value\" power/toughness or count is "
         "`TargetSpellManaValue`, read as the ability resolves."),
        ("lose life equal to its mana value, the stack entry's event value (The Ruinous Powers)",
         "lose life equal to its mana value, read as the ability resolves (`TargetSpellManaValue`; The Ruinous Powers)"),
        ("the amount can use the triggering spell's mana value via `EventValue` (Vial Smasher the Fierce)",
         "the amount is evaluated once as it resolves and can be the triggering spell's mana value, "
         "`TargetSpellManaValue` (Vial Smasher the Fierce)"),
    ],
    "EFFECTS_INDEX.md": [
        ("Implements `TriggeringSpellManaValueEffect` so a spell-cast trigger can carry `EventValue()` through the "
         "optional payment. Used by G'raha Tia, Scion Reborn.",
         "On a cast trigger the amount is `TargetSpellManaValue` (\"life equal to that spell's mana value\"). Used by "
         "G'raha Tia, Scion Reborn."),
        ("When used in `SpellCastTriggerEffect`, the collector snapshots the triggering spell's full mana value into "
         "the stack entry event value, so `EventValue` can size the token.",
         "On a cast trigger, `TargetSpellManaValue` sizes the token by the spell's mana value as the ability "
         "resolves."),
        ("The opponent choice is stored on the stack entry; the damage amount may use `EventValue`.",
         "The opponent choice is stored on the stack entry; the damage amount may be `TargetSpellManaValue`."),
    ],
    "ARCHITECTURE.md": [
        ("and `TargetSpellManaValue` falls back to it.",
         "and `TargetSpellManaValue` falls back to it. \"That spell's mana value\" is `TargetSpellManaValue` (the "
         "live spell, else that snapshot), never `EventValue`: the generic cast collector records the cast event's "
         "value on every entry and choice whatever the effects are (the kick count of a kicked-spell trigger, else "
         "the spell's mana value as the ability triggered), only a fallback where no snapshot is kept, and judges no "
         "effect by its class for it; the handlers that read the spell's mana value (cascade, Kellan, Living "
         "Breakthrough, Lady Loki, Ancient Cellarspawn, The Ruinous Powers, Baral and Kari Zev) evaluate "
         "`TargetSpellManaValue` too."),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("the reflexive graveyard target's X is the triggering spell's mana value (the entry's event value);",
         "the reflexive graveyard target's X is the triggering spell's mana value (the cast event's value the entry "
         "records as it triggers);"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if old in text:
                text = text.replace(old, new)
            else:
                raise SystemExit(f"{name}: expected text not found:\n{old}")
        path.write_text(text)
    for path in list(DOCS.rglob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        if re.search(r"\bTriggeringSpellManaValueEffect\b", path.read_text()):
            raise SystemExit(f"{path} still names TriggeringSpellManaValueEffect")
    print("docs updated")


if __name__ == "__main__":
    main()
