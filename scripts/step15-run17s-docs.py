#!/usr/bin/env python3
"""Step 15, run 17s: the agent docs show Hellish Rebuke's "the player who cast it" as an event condition, Curse of the
Cabal's "if the player does" as the recorded sacrifice count's ConditionalEffect, and the triggered mana ability marker.

Exact edits: each must apply once, or have been applied (idempotent). Fails when a doc still names
`DamagedPlayerOnlyEffect`.

Usage: step15-run17s-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

HELLISH_OLD = """| "Until end of turn, permanents your opponents control gain \\"When this permanent deals damage to the player who cast Hellish Rebuke, sacrifice this permanent. You lose 2 life.\\"" | `GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect(GrantTriggeredAbilityEffect(Triggers.dealsDamageToOpponent(), SequenceEffect.of(SacrificeSelfEffect(), LoseLifeEffect(2)), SELF))` | SPELL | Hellish Rebuke (AFC 26). The opponent-permanent scope is dynamic through end of turn; `Triggers.dealsDamageToOpponent()` covers combat and noncombat damage in the two-player engine. |"""
HELLISH_NEW = """| "Until end of turn, permanents your opponents control gain \\"When this permanent deals damage to the player who cast Hellish Rebuke, sacrifice this permanent. You lose 2 life.\\"" | `GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect(GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.dealsDamageToPlayer(), TriggerOptions.DEFAULT.withEventCondition(new TargetPlayerIs(null)), SequenceEffect.of(SacrificeSelfEffect(), LoseLifeEffect(2))), SELF))` | SPELL | Hellish Rebuke (AFC 26). The opponent-permanent scope is dynamic through end of turn. "To the player who cast Hellish Rebuke" is the event condition `TargetPlayerIs`, judged against the player dealt damage (combat and noncombat); the grant fills its `null` player with the caster as the spell resolves. |"""

CURSE_OLD = """| "At the beginning of each player's upkeep, if this card is suspended, that player may sacrifice a permanent of their choice. If the player does, put two time counters on this card." | `ConditionalEffect(SourceCardSuspended, MayEffect(SequenceEffect.of(SacrificePermanentsEffect(1, PermanentTruePredicate, ACTIVE_PLAYER).withRecordedSacrificeCount(), PutTimeCountersOnSuspendedCardEffect(2)), ..., ACTIVE_PLAYER))` | Triggers.upkeep(ActivePlayer.ANY) from exile | Curse of the Cabal (`TSP`). The trigger is collected from every suspended exiled card during every player's upkeep; the intervening condition is rechecked before the may choice, and counter placement is contingent on an actual sacrifice |"""
CURSE_NEW = """| "At the beginning of each player's upkeep, if this card is suspended, that player may sacrifice a permanent of their choice. If the player does, put two time counters on this card." | `TriggerOptions.DEFAULT.withInterveningIf(new SourceCardSuspended())` + `MayEffect(SequenceEffect.of(SacrificePermanentsEffect(1, PermanentTruePredicate, ACTIVE_PLAYER).withRecordedSacrificeCount(), ConditionalEffect(new EventValueAtLeast(1), PutTimeCountersOnSuspendedCardEffect(2))), ..., ACTIVE_PLAYER)` | `new TriggerCondition(Triggers.upkeep(ActivePlayer.ANY), AbilityZones.EXILE)` | Curse of the Cabal (`TSP`). The trigger is collected from every suspended exiled card during every player's upkeep; the intervening "if" is rechecked before the may choice, and "if the player does" reads the recorded sacrifice count |"""

EDITS = {
    DOCS / "ORACLE_TEXT_EFFECT_MAP.md": [
        (HELLISH_OLD, HELLISH_NEW),
        (CURSE_OLD, CURSE_NEW),
    ],
    DOCS / "EFFECTS_QUICK_REFERENCE.md": [
        ("""- `DamagedPlayerOnlyEffect(UUID playerId, CardEffect wrapped)` — gates a granted damage-to-player trigger on the damaged player being `playerId` (null is bound to the granting effect's controller by `GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect`); used by Hellish Rebuke (AFC 26).
""",
         """- "Whenever this deals damage to [one player]": the ability's event condition `TargetPlayerIs(UUID playerId)`, which the damage collectors judge against the player dealt damage; a `null` player is filled with the caster by `GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect` as it resolves (Hellish Rebuke, AFC 26).
"""),
        ("""- `PutTimeCountersOnSuspendedCardEffect(int amount)` - contingent follow-up for a suspended card's upkeep ability: after the active player actually sacrifices a permanent, adds the specified number of time counters to the still-suspended source card (Curse of the Cabal)
""",
         """- `PutTimeCountersOnSuspendedCardEffect(int amount)` - adds the specified number of time counters to the still-suspended source card; "if the player does [sacrifice]" wraps it in `ConditionalEffect(new EventValueAtLeast(1), …)` after `SacrificePermanentsEffect.withRecordedSacrificeCount()` (Curse of the Cabal), Pardic Dragon's "that player may put a time counter on it" in a `MayEffect` (`TRIGGERING_SPELL_CONTROLLER`)
"""),
        ("""- `TargetPlayerIsController()` is the target-player condition for "if that player is you" and can be used with `ConditionalEffect`.
""",
         """- `TargetPlayerIsController()` is the target-player condition for "if that player is you" and can be used with `ConditionalEffect`.
- `TargetPlayerIs(UUID playerId)` is "that player is [one player]": as a damage-to-a-player trigger's event condition it reads the player dealt damage (Hellish Rebuke's "the player who cast Hellish Rebuke", its `null` player filled by the grant).
"""),
        ("""- `ManaProducingEffect` — mana abilities (CR 605.1a). AI-estimator facets (default to neutral;
""",
         """- `TriggeredManaAbilityEffect` — an effect that adds mana as a "whenever a player taps a land for mana" ability resolves; with no target the ability is a triggered mana ability (CR 605.1b), resolved at once by the land-tap collector (CR 605.4a), so a judged intervening "if" no longer wraps it (Barbflare Gremlin). Implement it on a new land-tap mana effect.
- `ManaProducingEffect` — mana abilities (CR 605.1a). AI-estimator facets (default to neutral;
"""),
    ],
    DOCS / "EFFECTS_INDEX.md": [
        ("""`DamagedPlayerOnlyEffect` wraps a granted damage-to-player triggered effect so it fires only when the damaged player is the stated player (Hellish Rebuke).
""",
         """A granted damage-to-player ability that triggers only for one player is the event condition `TargetPlayerIs(playerId)`, judged against the player dealt damage (Hellish Rebuke).
"""),
        ("""| `PutTimeCountersOnSuspendedCardEffect` | `(int amount)` | contingent follow-up for a suspended-card upkeep ability: after the active player actually sacrifices a permanent, adds `amount` time counters to the still-suspended source card. Used by Curse of the Cabal |""",
         """| `PutTimeCountersOnSuspendedCardEffect` | `(int amount)` | adds `amount` time counters to the still-suspended source card. Curse of the Cabal's "if the player does" wraps it in `ConditionalEffect(new EventValueAtLeast(1), …)` after the recorded sacrifice; Pardic Dragon's "that player may" in a `MayEffect` |"""),
        ("""| `ManaProducingEffect` | `CardEffect` | Marks effects that produce mana. Used to identify mana abilities (CR 605.1a) without listing individual effect types. Implement this instead of `CardEffect` for new mana-producing effects. |
""",
         """| `ManaProducingEffect` | `CardEffect` | Marks effects that produce mana. Used to identify mana abilities (CR 605.1a) without listing individual effect types. Implement this instead of `CardEffect` for new mana-producing effects. |
| `TriggeredManaAbilityEffect` | `CardEffect` | Marks an effect that adds mana as a "whenever a player taps a land for mana" ability resolves; with no target the ability is a triggered mana ability (CR 605.1b) that the land-tap collector resolves at once (CR 605.4a), so `TapAbilityReader` takes a judged intervening "if" off it (Barbflare Gremlin). Implemented by the land-tap mana effects. |
"""),
    ],
}

FORBIDDEN = ["DamagedPlayerOnlyEffect"]


def main():
    failed = False
    for path, edits in EDITS.items():
        text = path.read_text()
        for old, new in edits:
            if text.count(old) == 1:
                text = text.replace(old, new)
            elif new not in text:
                print(f"{path}: edit not found: {old[:90]}")
                failed = True
        path.write_text(text)
    for path in DOCS.glob("*.md"):
        for number, line in enumerate(path.read_text().splitlines(), 1):
            for pattern in FORBIDDEN:
                if pattern in line:
                    print(f"{path}:{number}: still shows {pattern}")
                    failed = True
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
