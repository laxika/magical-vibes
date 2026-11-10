#!/usr/bin/env python3
"""Step 15, run 17z13e3: agent-docs follow the damage-to-a-player defaults.

Talon of Pain's "other than this artifact" is an event condition (`PutCountersOnSelfEffect`'s exclusion flag is gone),
Dragonborn Champion's "5 or more damage" an event condition with `DrawCardEffect`, "whenever a source you control
deals noncombat damage to an opponent" is its own trigger (not `opponentDealtNoncombatDamage()`, which is any source),
and the damage-to-a-player defaults record the event whatever the effects are. Fails if a doc still describes the
removed flag or the threshold draw on the damage trigger. Idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [(
        "| `PutCountersOnSelfEffect(CounterType.CHARGE, true)` | `Triggers.sourceYouControlDealsDamageToOpponent()` | "
        "Talon of Pain. The `true` flag excludes damage dealt by the watching artifact itself; the shared damage trigger "
        "carries the source permanent ID for that comparison |",
        "| `PutCountersOnSelfEffect(CounterType.CHARGE)` | `Triggers.sourceYouControlDealsDamageToOpponent()` with "
        "`TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new TriggeringPermanentMatches(new "
        "PermanentIsSourcePermanentPredicate())))` | Talon of Pain. \"Other than this\" is the event condition on the "
        "source of the damage (the entry's triggering permanent; a spell source has none and counts); \"whenever this "
        "deals damage to an opponent\" on the same trigger is `.withFilter(new PermanentIsSourcePermanentPredicate())` "
        "(Flesh Reaver) |",
    )],
    "EFFECTS_QUICK_REFERENCE.md": [(
        "Uses `DrawCardIfEventValueAtLeastEffect` with the damage event amount (Dragonborn Champion).",
        "\"Deals 5 or more damage\" is the event condition `EventValueAtLeast(5)` on the damage dealt "
        "(Dragonborn Champion, with `DrawCardEffect(1)`).",
    )],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("`PutCountersOnSelfEffect(CounterType, true)` excludes damage dealt by the watching permanent itself for "
         "\"another source\" wording (Talon of Pain).",
         "\"A source you control other than this\" is the event condition `NotCondition(TriggeringPermanentMatches("
         "PermanentIsSourcePermanentPredicate))` (Talon of Pain), \"this deals damage to an opponent\" the trigger's "
         "filter `PermanentIsSourcePermanentPredicate` (Flesh Reaver), \"5 or more damage\" `EventValueAtLeast(5)`."),
        ("| `Triggers.opponentDealtNoncombatDamage()` | Opponent dealt noncombat damage |",
         "| `Triggers.opponentDealtNoncombatDamage()` | Opponent dealt noncombat damage by any source (Chandra's "
         "Spitfire); \"a source you control deals noncombat damage to an opponent\" is "
         "`Triggers.sourceYouControlDealsNoncombatDamageToOpponent()` (Niv-Mizzet, Visionary; Chandra's Incinerator) |"),
    ],
    "ARCHITECTURE.md": [(
        "A damage-to-a-player trigger's \"that player\" — \"target creature that player controls\", "
        "\"destroy target land that player controls\" — is the player the event was about, never bound into the "
        "effect:",
        "A damage-to-a-player trigger's \"that player\" — \"target creature that player controls\", "
        "\"destroy target land that player controls\" — is the player the event was about, never bound into the "
        "effect (the damage-to-a-player watches' defaults, `DamageTriggerCollectorService.handlePlayerDamageDefault` "
        "and `handleOpponentSourceDamageDefault`, record the damage, the player, the source and its controller "
        "whatever the effects are; for \"a source an opponent controls deals damage to you\" the non-targeting "
        "\"that player\" is that opponent):",
    )],
}
STALE = {
    "ORACLE_TEXT_EFFECT_MAP.md": ["PutCountersOnSelfEffect(CounterType.CHARGE, true)"],
    "EFFECTS_QUICK_REFERENCE.md": ["Uses `DrawCardIfEventValueAtLeastEffect` with the damage event amount"],
    "ACTIVATED_ABILITY_GUIDE.md": ["`PutCountersOnSelfEffect(CounterType, true)`"],
}


def main():
    failed = False
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new not in text:
                if old not in text:
                    print(f"{name}: anchor not found: {old[:80]}")
                    failed = True
                    continue
                text = text.replace(old, new)
        path.write_text(text)
        for stale in STALE.get(name, []):
            if stale in text:
                print(f"{name}: still describes {stale}")
                failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
