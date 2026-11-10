#!/usr/bin/env python3
"""Step 15, run 17z14c3: the cards that say what their collectors judged or rebuilt by an effect's class (row 17z14c3).

Each card under `EDITS` was read against its Oracle text (Scryfall MCP; rulings checked):

- Vessel of the All-Consuming: "Whenever this creature deals damage to a player, if it has dealt 10 or more damage to
  that player this turn, they lose the game." The threshold was the collectors' check on
  `LoseGameIfSourceDealtDamageToPlayerThisTurnEffect`; it is the ability's intervening "if" (CR 603.4,
  `SourceDealtDamageToTargetPlayerThisTurn`) around the existing `TargetPlayerLosesGameEffect`.
- Ramses, Assassin Lord: "Whenever a player loses the game, if they were attacked this turn by an Assassin you
  controlled, you win the game." The collector judged `WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect`; it is
  the intervening "if" `TargetPlayerAttackedBySubtypeControllerControlledThisTurn` around `WinGameEffect`.
- Grip of Chaos: "Whenever a spell or ability is put onto the stack, if it has a single target, reselect its target at
  random." The collector judged `ReselectTargetAtRandomEffect`; it is the intervening "if"
  `TriggeringSpellMatches(StackEntryIsSingleTargetPredicate)`.
- The Pandorica: "When The Pandorica becomes untapped or leaves the battlefield, that permanent phases in" exists once
  the activated ability has phased a permanent out. The collector dropped `PhaseInLinkedPermanentEffect` when nothing
  was phased out; the untap ability's event condition (`SourceKeepsPermanentPhasedOut`) says it.
- Divine Intervention: "At the beginning of your upkeep, remove an intervention counter from this enchantment." and
  "When you remove the last intervention counter from this enchantment, the game is a draw." are two abilities; the
  second was the `thenEffect` of the first, which `PermanentCounterSupport` pulled out by class for the other ways to
  remove the counter. It is `Triggers.youRemoveLastCounterFromThis(CounterType.INTERVENTION)`.
- Captain Marvel, Apex Avenger: "Whenever you put one or more counters on another creature, if it's not a Kree, you may
  put the same number and kind of counters on Captain Marvel." "If it's not a Kree" was a flag of
  `PutSameCountersOnSourceEffect` its collector judged; it is the intervening "if". The effect reads the number and
  kind from the stack entry.
- Bold Plagiarist: "... they put the same number and kind of counters on this creature": the opponent puts them (its
  ruling), `PutSameCountersOnSourceEffect.putByTriggeringPlayer()`.
- Relic Bind: "Target player gains 1 life" declares its target, so the mode is never read as the non-targeting "that
  player" every "enchanted permanent becomes tapped" entry now carries.

Usage: step15-run17z14c3-cards.py [LETTER...] (run per letter module, `shared` for Vessel; no letter means all). Fails
if an edit didn't apply; idempotent.
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

# card -> [(old, new)]
EDITS = {
    "VesselOfTheAllConsuming": [(
        """        addTrigger(Triggers.dealsDamageToPlayer(),
                new LoseGameIfSourceDealtDamageToPlayerThisTurnEffect(10));""",
        """        addTrigger(Triggers.dealsDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceDealtDamageToTargetPlayerThisTurn(10)),
                new TargetPlayerLosesGameEffect(null));"""), (
        """import com.github.laxika.magicalvibes.model.effect.LoseGameIfSourceDealtDamageToPlayerThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.condition.SourceDealtDamageToTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
    "RamsesAssassinLord": [(
        """        addTrigger(Triggers.playerLosesGame(),
                new WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect(CardSubtype.ASSASSIN));""",
        """        addTrigger(Triggers.playerLosesGame(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TargetPlayerAttackedBySubtypeControllerControlledThisTurn(CardSubtype.ASSASSIN)),
                new WinGameEffect());"""), (
        """import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfPlayerAttackedByControlledSubtypeThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.condition.TargetPlayerAttackedBySubtypeControllerControlledThisTurn;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
    "GripOfChaos": [(
        """        addTrigger(Triggers.playerChoosesTargets(), new ReselectTargetAtRandomEffect());""",
        """        addTrigger(Triggers.playerChoosesTargets(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringSpellMatches(new StackEntryIsSingleTargetPredicate())),
                new ReselectTargetAtRandomEffect());"""), (
        """import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.condition.TriggeringSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ReselectTargetAtRandomEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
    "ThePandorica": [(
        """        addTrigger(Triggers.becomesUntapped(), new PhaseInLinkedPermanentEffect());""",
        """        addTrigger(Triggers.becomesUntapped(),
                TriggerOptions.DEFAULT.withEventCondition(new SourceKeepsPermanentPhasedOut()),
                new PhaseInLinkedPermanentEffect());"""), (
        """import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;""",
        """import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceKeepsPermanentPhasedOut;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;"""), (
        """import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
    "DivineIntervention": [(
        """        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterFromSourceThenEffect(
                        CounterType.INTERVENTION, new DrawGameEffect(), true));""",
        """        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterFromSourceEffect(CounterType.INTERVENTION, 1));
        addTrigger(Triggers.youRemoveLastCounterFromThis(CounterType.INTERVENTION), new DrawGameEffect());"""), (
        """import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;""",
        """import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;""")],
    "CaptainMarvelApexAvenger": [(
        """        addTrigger(Triggers.youPutCountersOnAnotherCreature(),
                new MayEffect(new PutSameCountersOnSourceEffect(true),""",
        """        addTrigger(Triggers.youPutCountersOnAnotherCreature(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new TriggeringPermanentHasSubtype(CardSubtype.KREE))),
                new MayEffect(new PutSameCountersOnSourceEffect(),"""), (
        """import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSameCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
    "BoldPlagiarist": [(
        """                new PutSameCountersOnSourceEffect());""",
        """                PutSameCountersOnSourceEffect.putByTriggeringPlayer());""")],
    "RelicBind": [(
        """                new ChooseOneEffect.ChooseOneOption("Target player gains 1 life",
                        new TargetPlayerGainsLifeEffect(1)))));""",
        """                new ChooseOneEffect.ChooseOneOption("Target player gains 1 life",
                        new TargetPlayerGainsLifeEffect(1),
                        new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player")))));"""), (
        """import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""",
        """import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;""")],
}


def find(name):
    for path in ROOT.glob(f"*/{PKG}/*/{name}.java"):
        return path.relative_to(ROOT).parts[0], path
    raise SystemExit(f"{name}: not found")


def main():
    letters = set(sys.argv[1:])
    for name, edits in EDITS.items():
        module, path = find(name)
        if letters and module not in letters:
            continue
        text = path.read_text()
        changed = False
        for old, new in edits:
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
            changed = True
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")


if __name__ == "__main__":
    main()
