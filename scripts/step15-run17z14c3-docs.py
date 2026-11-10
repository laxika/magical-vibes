#!/usr/bin/env python3
"""Step 15, run 17z14c3: `agent-docs` rows for the effects, conditions and trigger the run changed (row 17z14c3).

Each edit replaces the one line that starts with `prefix` by `line`, or inserts `line` after the one line that starts
with `prefix` (`after=True`). Fails if a prefix isn't found exactly once; idempotent (a line already there is skipped).
"""
import pathlib

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

# file -> [(prefix, line, after)]
EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `FlipCoinForTriggeringSpellAndCounterOnLossEffect` |",
         "| `FlipCoinForTriggeringSpellAndCounterOnLossEffect` | `()` | flips for the caster of the spell that caused the trigger and counters that spell if the caster loses; the cast trigger's entry is aimed at the triggering spell without targeting it, so the caster still flips once it has left the stack, and emits the caster's coin-flip win/loss triggers. Used by Mirrored Depths |",
         False),
        ("| `LoseGameIfSourceDealtDamageToPlayerThisTurnEffect` |",
         "| `LoseGameIfSourceDealtDamageToPlayerThisTurnEffect` | — | **Gone** (effect-slot-refactor.md step 15, run 17z14c3): Vessel of the All-Consuming writes `Triggers.dealsDamageToPlayer()` with `TriggerOptions.DEFAULT.withInterveningIf(new SourceDealtDamageToTargetPlayerThisTurn(10))` and `new TargetPlayerLosesGameEffect(null)`; the damaged player is the entry's non-targeting `targetId`, and the \"if\" is checked as the ability triggers and again as it resolves (CR 603.4) |",
         False),
        ("| `DrawGameEffect` |",
         "| `DrawGameEffect` | `()` | declares the game a draw when it resolves (Divine Intervention, on `Triggers.youRemoveLastCounterFromThis(CounterType.INTERVENTION)`) |",
         False),
        ("| `PutSameCountersOnSourceEffect` |",
         "| `PutSameCountersOnSourceEffect` | `()` or `PutSameCountersOnSourceEffect.putByTriggeringPlayer()` | \"put the same number and kind of counters on this\": reads the counter event from the stack entry as it resolves (`eventCounterType`, `eventValue`); the ability's controller puts them, or with `putByTriggeringPlayer()` the player who put the original counters (the entry's triggering player). The counter-placement collectors record the event on every entry whatever its effects are; \"if it's not a Kree\" is the card's intervening \"if\" (`NotCondition(TriggeringPermanentHasSubtype(KREE))`). Used by Captain Marvel, Apex Avenger and Bold Plagiarist |",
         False),
        ("| `ReselectTargetAtRandomEffect` |",
         "| `ReselectTargetAtRandomEffect` | `()` | global target-choice trigger body: reselects the triggering spell or ability's single target uniformly from all targets legal at resolution, including its current target. \"If it has a single target\" is the card's intervening \"if\" (`TriggeringSpellMatches(new StackEntryIsSingleTargetPredicate())`). Used by Grip of Chaos |",
         False),
        ("| `PhaseInLinkedPermanentEffect` |",
         "| `PhaseInLinkedPermanentEffect` | `()` | Phases in the permanent linked to the trigger source by `GameData.phasedOutWhileSourceTapped`, used when The Pandorica becomes untapped (with the event condition `SourceKeepsPermanentPhasedOut()`, so the ability exists only once a permanent was phased out) or leaves the battlefield |",
         False),
        ("`TargetPlayerAttackedControllerDuringLastTurn()` (",
         "`TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype)` (\"that player\" was attacked this turn by a creature of the subtype the source's controller controlled as it was declared; Ramses, Assassin Lord's intervening \"if\"), `SourceDealtDamageToTargetPlayerThisTurn(int minimum)` (the source has dealt at least that much damage to \"that player\" this turn; Vessel of the All-Consuming's intervening \"if\"), `SourceKeepsPermanentPhasedOut()` (a permanent is phased out by the source for as long as it remains tapped; The Pandorica's event condition),",
         True),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `DrawGameEffect()` —",
         "- `DrawGameEffect()` — declares the game a draw when it resolves (Divine Intervention, on `Triggers.youRemoveLastCounterFromThis(CounterType.INTERVENTION)`)",
         False),
        ("- `Triggers.timeCountersRemovedFromThis()` -",
         "- `Triggers.youRemoveLastCounterFromThis(CounterType kind)` - \"When you remove the last [kind] counter from this permanent\": triggers when the permanent's controller removes counters of that kind and none is left, however they are removed (`PermanentCounterSupport.notifyLastCounterRemoved`, called by `RemoveCounterFromSourceEffect`, the remove-all-counters effect and the counter-adjusting choice). The entry records the kind (`eventCounterType`) and the number removed (`eventValue`). Divine Intervention",
         True),
        ("- `CounterSpellingEffect` —",
         "- `CounterSpellingEffect` — marker for \"counter target spell\" (the AI's instant classifier reads it). A cast trigger with a counter goes on the stack like any other ability: aimed at the triggering spell without targeting it when the effect's spec admits a spell, else with the spell as its triggering card (Counterbalance, Swindler's Scheme, Dash Hopes, Temporal Extortion), so it still resolves after the spell was countered in response (it has no target, CR 608.2b). Impl `CounterSpellEffect`,",
         False),
        ("- `LoseGameIfSourceDealtDamageToPlayerThisTurnEffect(int minimumDamage)` —",
         "- `LoseGameIfSourceDealtDamageToPlayerThisTurnEffect` — **gone** (step 15, run 17z14c3): write `Triggers.dealsDamageToPlayer()` with `TriggerOptions.DEFAULT.withInterveningIf(new SourceDealtDamageToTargetPlayerThisTurn(minimum))` and `new TargetPlayerLosesGameEffect(null)` (Vessel of the All-Consuming); the damaged player is the entry's non-targeting `targetId` for combat and noncombat damage",
         False),
        ("- `PutSameCountersOnSourceEffect(",
         "- `PutSameCountersOnSourceEffect()` / `PutSameCountersOnSourceEffect.putByTriggeringPlayer()` — puts \"the same number and kind of counters\" as the counter event the ability triggered on, read from the stack entry as it resolves (`eventCounterType`, `eventValue`); the controller puts them, or the player who put the original counters. Captain Marvel, Apex Avenger (with the intervening \"if\" `NotCondition(TriggeringPermanentHasSubtype(KREE))`) and Bold Plagiarist",
         False),
        ("- `ReselectTargetAtRandomEffect()` —",
         "- `ReselectTargetAtRandomEffect()` — global target-choice trigger body: reselect the triggering spell or ability's single target uniformly from all targets that are legal when this effect resolves, including its current target; write \"if it has a single target\" as the intervening \"if\" `TriggeringSpellMatches(new StackEntryIsSingleTargetPredicate())` (Grip of Chaos)",
         False),
        ("- `PhaseInLinkedPermanentEffect()` —",
         "- `PhaseInLinkedPermanentEffect()` — phases in the target held by a source-tapped phasing effect when the source's untap/leave trigger resolves; the untap trigger takes the event condition `SourceKeepsPermanentPhasedOut()`. The Pandorica",
         False),
        ("`WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect(CardSubtype)` —",
         "`WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect` — **gone** (step 15, run 17z14c3): write `Triggers.playerLosesGame()` with `TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerAttackedBySubtypeControllerControlledThisTurn(subtype))` and `new WinGameEffect()` (Ramses, Assassin Lord). The loss-trigger stack entry carries the player who lost as non-targeting event context.",
         False),
        ("- `TargetPlayerAttackedControllerDuringLastTurn()` —",
         "- `TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype)` — \"that player\" (the event's or targeted player) was attacked this turn by a creature of the subtype the source's controller controlled, judged as the attacker was declared; intervening \"if\" of Ramses, Assassin Lord\n- `SourceDealtDamageToTargetPlayerThisTurn(int minimum)` — the source has dealt at least `minimum` damage this turn to \"that player\"; intervening \"if\" of Vessel of the All-Consuming\n- `SourceKeepsPermanentPhasedOut()` — a permanent is phased out by the source for as long as it remains tapped (`PhaseOutTargetPermanentWhileSourceTappedEffect`); event condition of The Pandorica's untap trigger",
         True),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever a player loses the game, if they were attacked this turn by an Assassin you controlled, you win the game\" |",
         "| \"Whenever a player loses the game, if they were attacked this turn by an Assassin you controlled, you win the game\" | `TriggerOptions.DEFAULT.withInterveningIf(new TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype.ASSASSIN))` + `new WinGameEffect()` | Triggers.playerLosesGame() | Ramses, Assassin Lord — loss-trigger stack entry carries the losing player as non-targeting event context; the intervening \"if\" is judged as the player loses and again as the ability resolves (CR 603.4) |",
         False),
        ("| \"Whenever you put one or more counters on another creature, if it's not a Kree, you may put the same number and kind of counters on Captain Marvel.\" |",
         "| \"Whenever you put one or more counters on another creature, if it's not a Kree, you may put the same number and kind of counters on Captain Marvel.\" | `TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TriggeringPermanentHasSubtype(CardSubtype.KREE)))` + `MayEffect(new PutSameCountersOnSourceEffect())` | `Triggers.youPutCountersOnAnotherCreature()` | Captain Marvel, Apex Avenger (MSC 78). The entry records the counter kind, number and creature of the placement event; the effect reads them as it resolves, and the non-Kree intervening \"if\" is judged as the ability triggers and again as it resolves (CR 603.4). Fires for creatures controlled by either player. |",
         False),
        ("| \"Whenever an opponent puts one or more counters on a creature they control, they put the same number and kind of counters on CARDNAME.\" |",
         "| \"Whenever an opponent puts one or more counters on a creature they control, they put the same number and kind of counters on CARDNAME.\" | `PutSameCountersOnSourceEffect.putByTriggeringPlayer()` | `Triggers.opponentPutsCountersOnCreatureTheyControl()` | Bold Plagiarist (C21 37). The trigger is controlled by Bold Plagiarist's controller, while the entry's triggering player (the opponent who put the counters) is the player who places the copied counters. |",
         False),
        ("| \"Whenever enchanted artifact becomes tapped, choose one — 1 damage to target player/planeswalker; or target player gains 1 life\" |",
         "| \"Whenever enchanted artifact becomes tapped, choose one — 1 damage to target player/planeswalker; or target player gains 1 life\" | `Triggers.enchantedPermanentBecomesTapped()` `new ChooseOneEffect(List.of(new ChooseOneOption(\"This Aura deals 1 damage to target player or planeswalker\", new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)), new ChooseOneOption(\"Target player gains 1 life\", new TargetPlayerGainsLifeEffect(1), new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY), \"Target must be a player\"))))` | Trigger | Relic Bind. The enchanted-tap default puts the modal on the stack as written, with the same record as any other ability of the watch; its mode, then that mode's target, is announced there (`StackedModalTriggerService`, CR 603.3c). A player-only mode declares its `PlayerPredicateTargetFilter`, or it would be read as the entry's non-targeting \"that player\" |",
         False),
        ("| \"This enchantment enters with two intervention counters on it. At the beginning of your upkeep, remove an intervention counter from this enchantment. When you remove the last intervention counter from this enchantment, the game is a draw.\" |",
         "| \"This enchantment enters with two intervention counters on it. At the beginning of your upkeep, remove an intervention counter from this enchantment. When you remove the last intervention counter from this enchantment, the game is a draw.\" | `EnterWithCountersEffect(INTERVENTION, Fixed(2))` + `RemoveCounterFromSourceEffect(INTERVENTION, 1)` + `DrawGameEffect()` | STATIC + Triggers.upkeep(ActivePlayer.YOU) + Triggers.youRemoveLastCounterFromThis(CounterType.INTERVENTION) | Divine Intervention. Three abilities; the third triggers when its controller removes the last intervention counter, however that happens (its ruling), and an upkeep with no counters is a no-op |",
         False),
        ("| \"Whenever ~ deals damage to a player, if it has dealt 10 or more damage to that player this turn, they lose the game.\" |",
         "| \"Whenever ~ deals damage to a player, if it has dealt 10 or more damage to that player this turn, they lose the game.\" | `TriggerOptions.DEFAULT.withInterveningIf(new SourceDealtDamageToTargetPlayerThisTurn(10))` + `new TargetPlayerLosesGameEffect(null)` | Triggers.dealsDamageToPlayer() | Vessel of the All-Consuming (NEO). The per-source, per-player damage total is the intervening \"if\", judged as the ability triggers and again as it resolves (CR 603.4); the entry's non-targeting `targetId` is the player who was dealt damage, and the normal loss handler applies at resolution |",
         False),
        ("| \"Whenever a spell or ability is put onto the stack, if it has a single target, reselect its target at random\" |",
         "| \"Whenever a spell or ability is put onto the stack, if it has a single target, reselect its target at random\" | `TriggerOptions.DEFAULT.withInterveningIf(new TriggeringSpellMatches(new StackEntryIsSingleTargetPredicate()))` + `ReselectTargetAtRandomEffect()` | `Triggers.playerChoosesTargets()` | Grip of Chaos (SCG). The existing global target-choice hook covers both spells and activated/triggered abilities; the intervening \"if\" is judged on the spell or ability as its targets are chosen and again as the ability resolves (CR 603.4), and the handler recomputes all legal targets at resolution, including the current target |",
         False),
    ],
}


# file -> [(old, new)], replaced inside a line
REWORDS = {
    "EFFECTS_INDEX.md": [
        (" Its `targetsTriggeringSpell()` is `false`: the ability doesn't target the spell, so it still resolves after "
         "the spell was countered in response (CR 608.2b).",
         " The ability doesn't target the spell (the spell is its entry's triggering card), so it still resolves after "
         "the spell was countered in response (CR 608.2b)."),
        (" Its `targetsTriggeringSpell()` is `false`, as Counterbalance's is.",
         " It doesn't target the spell either, as Counterbalance's doesn't."),
    ],
}


def main():
    for name, rewords in REWORDS.items():
        path = DOCS / name
        text = path.read_text()
        for old, new in rewords:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: {old!r} not found once")
            text = text.replace(old, new)
            path.write_text(text)
            print(f"reworded {name}")
    for name, edits in EDITS.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        changed = False
        for prefix, line, after in edits:
            if all(part in lines for part in line.split("\n")):
                continue
            hits = [i for i, existing in enumerate(lines) if existing.startswith(prefix)]
            if len(hits) != 1:
                raise SystemExit(f"{name}: {prefix!r} found {len(hits)} times")
            if after:
                lines[hits[0] + 1:hits[0] + 1] = line.split("\n")
            else:
                lines[hits[0]] = line
            changed = True
        if changed:
            path.write_text("\n".join(lines))
            print(f"edited {name}")
    for name in EDITS:
        text = (DOCS / name).read_text()
        for stale in ("targetsTriggeringSpell", "requiresNonKree"):
            if stale in text:
                raise SystemExit(f"{name}: still mentions {stale}")


if __name__ == "__main__":
    main()
