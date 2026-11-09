#!/usr/bin/env python3
"""Step 15, run 17z11f6: the cards whose abilities are written as printed instead of bound, unwrapped or rebuilt by an
effect's class.

Idempotent; per letter module. Optional repository-root argument (default: this script's repository).
"""
import pathlib
import sys

ROOT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"


def card(letter, name):
    return CARDS / letter / "src/main/java/com/github/laxika/magicalvibes/cards" / letter / f"{name}.java"


def edit(path, pairs, imports_add=(), imports_remove=()):
    s = path.read_text()
    orig = s
    for old, new in pairs:
        if new in s and old not in s:
            continue
        if old not in s:
            raise SystemExit(f"{path.name}: missing {old!r}")
        s = s.replace(old, new)
    for imp in imports_remove:
        line = f"import {imp};\n"
        s = s.replace(line, "")
    for imp in imports_add:
        line = f"import {imp};\n"
        if line not in s:
            first = s.index("import ")
            s = s[:first] + line + s[first:]
    if s != orig:
        path.write_text(s)
        print("edited", path.name)


EDITS = []


def main():
    for args in EDITS:
        edit(*args)


# Calix, Guided by Fate: "do this only once each turn" is the ability's TriggerLimit.DO_ONLY_ONCE_EACH_TURN, which marks
# the stack entry; the copy effect carries no flag of its own.
EDITS.append((card("c", "CalixGuidedByFate"),
              [("new CreateTokenCopyOfChosenPermanentYouControlEffect(\n"
                "                                NONLEGENDARY_ENCHANTMENT, true),",
                "new CreateTokenCopyOfChosenPermanentYouControlEffect(NONLEGENDARY_ENCHANTMENT),")]))

# Strict Proctor: "whenever a permanent entering causes a triggered ability to trigger, counter that ability unless its
# controller pays {2}" is a triggered ability, not a static marker the enters service rebuilt into a counter.
EDITS.append((card("s", "StrictProctor"),
              [("        addStaticEffect(new StrictProctorEffect());",
                "        addTrigger(Triggers.permanentEnteringCausesAbilityToTrigger(), new CounterUnlessPaysEffect(2));")],
              ["com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect",
               "com.github.laxika.magicalvibes.model.trigger.Triggers"],
              ["com.github.laxika.magicalvibes.model.effect.StrictProctorEffect"]))

# Cephalid Constable: "return up to that many target permanents that player controls" declares its targets, chosen as
# the ability goes on the stack, instead of the damage collectors baking the targeted form in.
EDITS.append((card("c", "CephalidConstable"),
              [("addTrigger(Triggers.dealsCombatDamageToPlayer(), new ReturnPermanentsOnCombatDamageToPlayerEffect());",
                "addTrigger(Triggers.dealsCombatDamageToPlayer(),\n"
                "                ReturnPermanentsOnCombatDamageToPlayerEffect.upToThatManyTargets());")]))

# Balthier and Fran: "whenever a Vehicle crewed by Balthier and Fran this turn attacks, if it's the first combat phase
# of the turn, you may pay {1}{R}{G}" is an attack trigger of the card, its filter the Vehicle it crewed this turn and
# its intervening "if" the first combat phase, not a crew trigger registering a delayed watcher.
EDITS.append((card("b", "BalthierAndFran"),
              [("        addTrigger(Triggers.crewsVehicle(), new RegisterDelayedVehicleAttackEffect(\n"
                "                new MayPayManaEffect(\"{1}{R}{G}\", new AdditionalCombatPhaseEffect(1),\n"
                "                        \"Pay {1}{R}{G} to get an additional combat phase?\")));",
                "        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentAllOfPredicate(List.of(\n"
                "                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE),\n"
                "                        new PermanentCrewedThisTurnBySourcePredicate()))),\n"
                "                TriggerOptions.DEFAULT.withInterveningIf(new FirstCombatPhase()),\n"
                "                new MayPayManaEffect(\"{1}{R}{G}\", new AdditionalCombatPhaseEffect(1),\n"
                "                        \"Pay {1}{R}{G} to get an additional combat phase?\"));")],
              ["com.github.laxika.magicalvibes.model.condition.FirstCombatPhase",
               "com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate",
               "com.github.laxika.magicalvibes.model.filter.PermanentCrewedThisTurnBySourcePredicate",
               "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
               "java.util.List"],
              ["com.github.laxika.magicalvibes.model.effect.RegisterDelayedVehicleAttackEffect"]))

# Apple of Eden, Isu Relic: "until end of turn, whenever you play a land or cast a spell this way, its owner draws a
# card" are registered abilities whose filters are "a card exiled with this", the permanent that registered them (CR
# 603.7e), and whose effect draws for the played card's owner; no descriptor bound to the source and target.
EDITS.append((card("a", "AppleOfEdenIsuRelic"),
              [("        List<CardEffect> ownerDrawTrigger = List.of(\n"
                "                new DrawCardForTargetPlayerEffect(1));\n",
                "        CardEffect ownerDraws = DrawCardForTargetPlayerEffect.ofTriggeringCardOwner(1);\n"),
               ("                                new TriggeredAbility(Triggers.youCastSpell(),\n"
                "                                new PlayFromOutsideHandTriggerEffect(ownerDrawTrigger))),",
                "                                new TriggeredAbility(Triggers.youCastSpell().exiledWithThis(), ownerDraws)),"),
               ("                                new TriggeredAbility(Triggers.youPlayLand(),\n"
                "                                new PlayFromOutsideHandTriggerEffect(ownerDrawTrigger)))",
                "                                new TriggeredAbility(Triggers.youPlayLand().exiledWithThis(), ownerDraws))")],
              [],
              ["com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect"]))

# Nuka-Nuke Launcher: "until the end of defending player's next turn, that player gets two rad counters whenever they
# cast a spell": the registration records the defending player, the ability's filter is a spell cast by that player and
# its duration that player's next turn; "that player" is the caster the collector records.
EDITS.append((card("n", "NukaNukeLauncher"),
              [("                new RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect(\n"
                "                        new TriggeredAbility(Triggers.playerCastsSpell(),\n"
                "                        new TargetPlayerSpellCastTriggerEffect(\n"
                "                                List.of(new GiveTargetPlayerRadCountersEffect(2)))),\n"
                "                        null));",
                "                RegisterGlobalTriggeredAbilityUntilEndOfNextTurnEffect.untilWatchedPlayersNextTurn(\n"
                "                        new TriggeredAbility(Triggers.playerCastsSpell()\n"
                "                                .withStackEntryFilter(new StackEntryControlledByWatchedPlayerPredicate()),\n"
                "                                new GiveTargetPlayerRadCountersEffect(2))));")],
              ["com.github.laxika.magicalvibes.model.filter.StackEntryControlledByWatchedPlayerPredicate"],
              ["com.github.laxika.magicalvibes.model.effect.TargetPlayerSpellCastTriggerEffect", "java.util.List"]))

EDITS.append((card("n", "NukaNukeLauncher"),
              [("import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\n\n@CardRegistration",
                "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n\n@CardRegistration")]))

if __name__ == "__main__":
    main()
