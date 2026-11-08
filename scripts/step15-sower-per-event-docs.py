#!/usr/bin/env python3
"""Step 15, follow-up after run 17y5: the agent-docs name Sower of Discord's per-event trigger.

Sower of Discord moved from `Triggers.sourceDealsDamageToPermanentOrPlayer()` (once for each source and recipient) to
the new `Triggers.playerDealtDamage()` (once for each player and damage event, every source's damage summed). The docs
that named Sower's form say so; the "deals damage to a permanent or player" rows lose their Sower example and gain a
pointer, and `ACTIVATED_ABILITY_GUIDE.md` gets a row for the new trigger.

Idempotent: an edit already made is left alone. Fails if a doc still names Sower on the old trigger.

Usage: step15-sower-per-event-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

OLD_FORM = "`Triggers.sourceDealsDamageToPermanentOrPlayer().withDamagedPlayerFilter(new PlayerChosenBySourcePredicate())`"
NEW_FORM = "`Triggers.playerDealtDamage().withDamagedPlayerFilter(new PlayerChosenBySourcePredicate())`"

EDITS = {
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| STATIC + `Triggers.sourceDealsDamageToPermanentOrPlayer().withDamagedPlayerFilter(new PlayerChosenBySourcePredicate())` "
         "| Sower of Discord (CMM 187). The as-enters choice remembers both players; the trigger fires once for each "
         "source and chosen player dealt damage, that player the entry's triggering player and the damage its event "
         "value. |",
         "| STATIC + `Triggers.playerDealtDamage().withDamagedPlayerFilter(new PlayerChosenBySourcePredicate())` "
         "| Sower of Discord (CMM 187). The as-enters choice remembers both players; the trigger fires once for each "
         "chosen player and damage event, however many sources deal the damage at once (Boros Reckoner's ruling), "
         "that player the entry's triggering player and the total damage its event value. |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- \"Whenever damage is dealt to one of the chosen players, the other chosen player also loses that much life\" "
         "— " + OLD_FORM + " + `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)`: once for each "
         "source and chosen player, that player the entry's triggering player (Sower of Discord).",
         "- \"Whenever damage is dealt to one of the chosen players, the other chosen player also loses that much life\" "
         "— " + NEW_FORM + " + `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)`: once for each "
         "chosen player and damage event, every source's damage summed, that player the entry's triggering player "
         "(Sower of Discord)."),
        ("- `OtherChosenPlayerLosesLifeEffect` — **Gone** (effect-slot-refactor.md step 15, run 17y4): Sower of Discord "
         "writes " + OLD_FORM + " + `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)`",
         "- `OtherChosenPlayerLosesLifeEffect` — **Gone** (effect-slot-refactor.md step 15, run 17y4): Sower of Discord "
         "writes " + NEW_FORM + " + `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)` (the "
         "per-event trigger since the follow-up after run 17y5)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `OtherChosenPlayerLosesLifeEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17y4): Sower of "
         "Discord writes " + OLD_FORM + " + `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)` |",
         "| `OtherChosenPlayerLosesLifeEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17y4): Sower of "
         "Discord writes " + NEW_FORM + " + `LoseLifeEffect(new EventValue(), LoseLifeRecipient.OTHER_CHOSEN_PLAYER)` "
         "(the per-event trigger since the follow-up after run 17y5) |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.sourceDealsDamageToPermanentOrPlayer()` / `anotherSourceYouControlDealsDamageToPermanentOrPlayer()` "
         "| \"Whenever [another source you control] deals damage to a permanent or player\" / \"whenever damage is dealt "
         "to …\" — once for each source and each permanent and player it deals damage to (Ghyrson Starn's ruling): the "
         "recipient is the entry's non-targeting `targetId` (a player also its triggering player, a permanent its "
         "triggering permanent), the damage dealt to it the event value. `withFilter` / `withSpellFilter` narrow the "
         "source, `withDamagedFilter` / `withDamagedPlayerFilter` the recipient (Sower of Discord: "
         "`PlayerChosenBySourcePredicate`); \"exactly 1 damage\" is an event condition on the event value |",
         "| `Triggers.sourceDealsDamageToPermanentOrPlayer()` / `anotherSourceYouControlDealsDamageToPermanentOrPlayer()` "
         "| \"Whenever [another source you control] deals damage to a permanent or player\" — once for each source and "
         "each permanent and player it deals damage to (Ghyrson Starn's ruling): the recipient is the entry's "
         "non-targeting `targetId` (a player also its triggering player, a permanent its triggering permanent), the "
         "damage dealt to it the event value. `withFilter` / `withSpellFilter` narrow the source, `withDamagedFilter` / "
         "`withDamagedPlayerFilter` the recipient; \"exactly 1 damage\" is an event condition on the event value. "
         "\"Whenever damage is dealt to [a player]\" is `Triggers.playerDealtDamage()` |\n"
         "| `Triggers.playerDealtDamage()` | \"Whenever damage is dealt to a player\" — once for each player and damage "
         "event, however many sources deal it at once (CR 603.2c; Boros Reckoner's ruling; all the combat damage of a "
         "combat damage step is one event, as are the simultaneous sources of one resolution damage event): the player "
         "is the entry's non-targeting `targetId` and triggering player, the total damage dealt to them in the event the "
         "event value. `withDamagedPlayerFilter` narrows the player (Sower of Discord: `PlayerChosenBySourcePredicate`); "
         "it names no source, so it takes no `withFilter` |"),
    ],
    "PREDICATES_REFERENCE.md": [
        ("Sower of Discord's \"one of the chosen players\" as a damage trigger's `withDamagedPlayerFilter` |",
         "Sower of Discord's \"one of the chosen players\" as `Triggers.playerDealtDamage()`'s `withDamagedPlayerFilter` |"),
    ],
    "ARCHITECTURE.md": [
        ("which an event condition reads as it triggers (Ghyrson Starn's \"exactly 1 damage\").",
         "which an event condition reads as it triggers (Ghyrson Starn's \"exactly 1 damage\"). A "
         "`Triggers.playerDealtDamage()` ability (Sower of Discord) triggers once for each player and damage event "
         "(`TriggerCollectionService.checkPlayerDealtDamageTriggers`), every source's damage to that player summed: "
         "`CombatDamageService` sums one combat damage step's, before the state-based actions; "
         "`DamageSupport.flushSourceDamageReflections` sums the noncombat batches (`PendingSourceDamage`) that share a "
         "resolution damage event (`PendingSourceDamage.getDamageEvent()`, the `GameData.openDamageEvent` the batch "
         "began in)."),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: the line to edit was not found: {old[:80]}…")
            text = text.replace(old, new, 1)
            print(f"edited {name}")
        path.write_text(text)
    for path in DOCS.glob("*.md"):
        for line in path.read_text().splitlines():
            if "Sower of Discord" in line and "sourceDealsDamageToPermanentOrPlayer()" in line \
                    and "playerDealtDamage()" not in line:
                raise SystemExit(f"{path.name} still names Sower on the old trigger: {line[:120]}…")


if __name__ == "__main__":
    main()
