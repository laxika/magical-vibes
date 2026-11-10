#!/usr/bin/env python3
"""Step 15, run 17z14g: `agent-docs` rows for the effects and cards the run changed (row 17z14g).

Each edit replaces the one line that starts with `prefix` by `line`, or removes it (`line` is `None`). Fails if a
prefix isn't found exactly once; idempotent (a line already there, or already removed, is skipped).
"""
import pathlib

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

IF_NOT_RENOWNED = "TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned()))"

# file -> [(prefix, line)]
EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ConjureDuplicateOfCombatDamageSourceIntoHandEffect()` -",
         "- `ConjureDuplicateOfCombatDamageSourceIntoHandEffect()` - on a combat-damage-to-player trigger, conjures a fresh duplicate of the nontoken damage-dealing creature (the entry's triggering permanent, else the source) into the trigger controller's hand. \"If it's not a token\" is the card's intervening \"if\" on the creature that dealt the damage, `TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TriggeringPermanentMatches(new PermanentIsTokenPredicate())))`, which the combat-damage collectors judge with that creature as the triggering permanent. Used by Semblance Scanner (YNEO 9)."),
        ("- `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` —",
         "- `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` — \"exile target instant or sorcery card from your graveyard chosen at random\": the effect declares the graveyard target (`targetSpec()`) and that it is chosen at random (`targetChosenAtRandom()`), so the combat-damage collector picks it as the trigger goes on the stack (the entry's `targetId`, with every effect of the ability; no legal card, no trigger; no effect if it left the graveyard), then offers it for a free cast at that controller's next upkeep; if cast, it is exiled instead of going to a graveyard"),
        ("- `RegisterDelayedWatchedCreaturesCombatDamageEffect(List<CardEffect> effects)` /",
         "- `RegisterDelayedWatchedCreaturesCombatDamageEffect(List<CardEffect> effects)` / `(List<CardEffect> effects, boolean combatDamageToPlayerOnly)` / `(List<CardEffect> effects, boolean combatDamageToPlayerOnly, boolean untilEndOfTurn)` / `RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn(List<CardEffect> effects)` — \"Choose up to two target creatures. Until your next turn, whenever either of those creatures deals combat damage, <effects>\" (Tamiyo, Field Researcher +1, with `DrawCardEffect(1)`). Registers a `DelayedWatchedCreaturesCombatDamage` keyed by the chosen permanent ids; `CombatDamageService` fires one non-targeting trigger per watched creature that dealt positive combat damage to anything (player, planeswalker, or creature), or only to a player when the boolean is `true` (Captain Howler, Sea Scourge). The trigger is controlled by the player who activated the ability, so \"you draw\" draws for them even when the watched creature is an opponent's. It expires at cleanup when `untilEndOfTurn` is `true`, otherwise at that controller's next turn start. `forChosenCreatureThisTurn` watches the creature the resolving ability chose (`StackEntry.getChosenPermanentId()`, after `BolsterEffect(..., true)`) instead of a target, for combat damage to a player, until end of turn: \"when that creature deals combat damage to a player this turn, convert Optimus Prime\" (CR 603.7). The trigger's entry names the permanent whose ability registered the watch as its granting permanent."),
        ("- `RenownEffect(int amount)` —",
         "- `RenownEffect(int amount)` — renown N (CR 702.112a) on `Triggers.dealsCombatDamageToPlayer()` with `" + IF_NOT_RENOWNED + "`, renown's intervening \"if it isn't renowned\" (a granted renown is `new TriggeredAbility(Triggers.dealsCombatDamageToPlayer(), <those options>, new RenownEffect(N))`); counters + `Permanent.renowned`, read back by the `SourceIsRenowned` condition; the flip fires `Triggers.becomesRenowned()` (Relic Seeker) and `Triggers.creatureYouControlBecomesRenowned()` (Valeron Wardens) triggers"),
        ("- `GrantEffectToChosenCreatureUntilEndOfTurnEffect(EffectSlot, CardEffect)` —", None),
        ("- `TransformGrantingPermanentEffect()` —",
         "- `TransformGrantingPermanentEffect()` — toggles the face of the permanent that granted or created this ability (the stack entry's granting permanent); use in `RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn(...)` when the chosen creature's later combat damage transforms the original source (Optimus Prime, Autobot Leader)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ConjureDuplicateOfCombatDamageSourceIntoHandEffect` |",
         "| `ConjureDuplicateOfCombatDamageSourceIntoHandEffect` | `()` | On a combat-damage-to-player trigger, conjures a fresh duplicate of the nontoken damage-dealing creature into the trigger controller's hand: the entry's triggering permanent as it dealt the damage (the equipped creature, which the attached collector records), else the source. \"If it's not a token\" is the card's intervening \"if\", `NotCondition(TriggeringPermanentMatches(PermanentIsTokenPredicate))`, judged by the combat-damage collectors with the creature that dealt the damage as the triggering permanent. | Semblance Scanner (YNEO 9) |"),
        ("| `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect` |",
         "| `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect` | `()` | declares \"target instant or sorcery card from your graveyard\" (`targetSpec()`) chosen at random (`targetChosenAtRandom()`): the combat-damage collector picks it as the trigger goes on the stack (the entry's `targetId`, in the graveyard; no legal card, no trigger), and the effect exiles it from the controller's graveyard and queues the exiled card for a free-cast offer at that controller's next upkeep; a cast card is exiled instead of going to a graveyard |"),
        ("| `RenownEffect` |",
         "| `RenownEffect` | `(int amount)` | renown N (CR 702.112a): Triggers.dealsCombatDamageToPlayer() with `" + IF_NOT_RENOWNED + "` (the keyword's intervening \"if it isn't renowned\", judged as the ability triggers and again as it resolves, CR 603.4) — the source creature gets N +1/+1 counters and becomes renowned (`Permanent.renowned`). Acts on the entry's source, the creature. Already-renowned creatures do nothing (CR 702.112c), and the creature still becomes renowned when counters can't be placed. Read back with the `SourceIsRenowned` condition. Flipping a creature to renowned also fires `Triggers.becomesRenowned()` (\"when this creature becomes renowned\", Relic Seeker) and `Triggers.creatureYouControlBecomesRenowned()` (\"whenever a creature you control becomes renowned\", Valeron Wardens) via `TriggerCollectionService.checkBecomesRenownedTriggers`. Consul's Lieutenant |"),
        ("| `GrantEffectToChosenCreatureUntilEndOfTurnEffect` |", None),
        ("| `TransformGrantingPermanentEffect` |",
         "| `TransformGrantingPermanentEffect` | `()` | transforms the permanent that granted or created this ability, the stack entry's granting permanent; `RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn` names the registering ability's source as the delayed trigger's granting permanent (Optimus Prime, Autobot Leader) |"),
        ("| `RegisterDelayedWatchedCreaturesCombatDamageEffect` |",
         "| `RegisterDelayedWatchedCreaturesCombatDamageEffect` | `(List<CardEffect> effects)` / `(List<CardEffect> effects, boolean combatDamageToPlayerOnly)` / `(List<CardEffect> effects, boolean combatDamageToPlayerOnly, boolean untilEndOfTurn)` / `.forChosenCreatureThisTurn(List<CardEffect> effects)` | registers a delayed triggered ability against this effect's target creatures: \"until your next turn, whenever either of those creatures deals combat damage, <effects>\". Targets creatures (`TargetSpec.benign`); pair with a multi-target ability for \"up to two\". Stored in `GameData.delayedActions` (`DelayedWatchedCreaturesCombatDamage`), fired in `CombatDamageService` once per watched creature per combat damage step (damage to anything counts by default; pass `true` for player-only damage), controlled by the player who resolved the effect, and expired at cleanup when `untilEndOfTurn` is true or at that controller's next turn start otherwise. `forChosenCreatureThisTurn` has no target: it watches the creature the resolving ability chose (`StackEntry.getChosenPermanentId()`) for combat damage to a player until end of turn (\"when that creature deals combat damage to a player this turn, …\", CR 603.7). Each trigger's entry names the registering ability's source permanent as its granting permanent. Used by Tamiyo, Field Researcher's +1, Captain Howler, Sea Scourge and Optimus Prime, Autobot Leader |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Renown N\" |",
         "| \"Renown N\" | `" + IF_NOT_RENOWNED + "` + `RenownEffect(N)` | Triggers.dealsCombatDamageToPlayer() | CR 702.112a: \"when this creature deals combat damage to a player, if it isn't renowned, …\". Sets `Permanent.renowned`; only the first instance does anything |"),
        ("| \"Whenever this creature deals combat damage to a player, exile target instant or sorcery card from your graveyard chosen at random.",
         "| \"Whenever this creature deals combat damage to a player, exile target instant or sorcery card from your graveyard chosen at random. At the beginning of your next upkeep, you may cast that card without paying its mana cost. If that spell would be put into your graveyard, exile it instead.\" | `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` | `Triggers.dealsCombatDamageToPlayer()` | Power Pack (MSC 90). The effect declares its graveyard target and that it is chosen at random; the collector selects it when the combat-damage trigger is put on the stack (the entry's `targetId`; no legal card, no trigger) and nothing happens if it left the graveyard; the delayed offer uses the shared next-upkeep free-cast pipeline and marks the cast card for exile instead of a graveyard |"),
        ("| \"Whenever you attack, bolster 2. The chosen creature gains trample until end of turn. When that creature deals combat damage to a player this turn, convert Optimus Prime.\" |",
         "| \"Whenever you attack, bolster 2. The chosen creature gains trample until end of turn. When that creature deals combat damage to a player this turn, convert Optimus Prime.\" | `SequenceEffect.of(BolsterEffect(2, true), GrantKeywordToChosenCreatureUntilEndOfTurnEffect(TRAMPLE, null), RegisterDelayedWatchedCreaturesCombatDamageEffect.forChosenCreatureThisTurn(List.of(new TransformGrantingPermanentEffect())))` | Triggers.youAttack() | Optimus Prime, Autobot Leader (BOT 13). The bolster choice is retained on the resolving entry so both the trample grant and the delayed combat-damage trigger refer to the same creature; the delayed triggered ability is Optimus Prime's (CR 603.7e), not the chosen creature's, so nothing that adds triggers for that creature's abilities copies it |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Combat damage → random own-graveyard instant/sorcery, free cast at next upkeep |",
         "| Combat damage → random own-graveyard instant/sorcery, free cast at next upkeep | `p/PowerPack.java` | Triggers.dealsCombatDamageToPlayer() `ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect()` — the effect declares its graveyard target as chosen at random, so the collector picks the card as the trigger goes on the stack and it is the entry's `targetId` (graveyard zone); the ability does nothing if that card has left the graveyard, then exiled and offered through the shared rebound-style next-upkeep free-cast trigger; cast cards are exiled instead of going to a graveyard |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| combat damage by this creature or its equipped creature conjures a duplicate of the nontoken damage source into hand |",
         "| combat damage by this creature or its equipped creature conjures a duplicate of the nontoken damage source into hand | `s/SemblanceScanner.java` + `ConjureDuplicateOfCombatDamageSourceIntoHandEffect`, \"if it's not a token\" as the intervening \"if\" `NotCondition(TriggeringPermanentMatches(PermanentIsTokenPredicate))` |"),
    ],
}


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        changed = False
        for prefix, line in edits:
            found = [index for index, existing in enumerate(lines) if existing.startswith(prefix)]
            if line is None:
                if not found:
                    continue
                if len(found) != 1:
                    raise SystemExit(f"{name}: {len(found)} lines start with {prefix!r}")
                del lines[found[0]]
                changed = True
                continue
            if len(found) != 1:
                raise SystemExit(f"{name}: {len(found)} lines start with {prefix!r}")
            if lines[found[0]] != line:
                lines[found[0]] = line
                changed = True
        if changed:
            path.write_text("\n".join(lines))
            print(f"edited {name}")


if __name__ == "__main__":
    main()
