#!/usr/bin/env python3
"""Step 15, run 17k: the "a player attacks" and "a creature attacks" cards are written with triggers, filters and event
conditions, so their collectors judge no conditional by its condition's type.

- "Whenever a player attacks one of your opponents" (Combat Calligrapher, Ellie, Brick Master; Breena, the Demagogue;
  Squall, Gunblade Duelist's "whenever one or more creatures attack one of your opponents") is
  `Triggers.playerAttacksOneOfYourOpponents()`, once for each opponent attacked, where `Triggers.playerAttacks()` with
  `PlayerAttacksOneOfYourOpponents` fanned out by the condition's class. Karazikar's "whenever an opponent attacks
  another one of your opponents" is `Triggers.opponentAttacksAnotherOpponent()`. Breena's and Squall's intervening
  "if"s stay (CR 603.4), Squall's renamed `AttackersHavePowerOrToughnessEqualToSourceChosenNumber`.
- Killian, Decisive Mentor's "whenever one or more creatures that are enchanted by an Aura you control attack" is the
  trigger's filter (`Triggers.playerAttacks().withFilter(...)`), as run 17j's "you attack" filter form.
- A condition of a "whenever a player attacks" trigger event written as an intervening "if" is the ability's event
  condition (run 17i's rule; the Oracle scan's EVENT list, `step15-run17j-scan.py`): Archnemesis, Aurelia, Cunning
  Rhetoric, the eight Curses, Everett K. Ross, Firemane Commando, Flummoxed Cyclops, Jolene, Mila, Tenuous Truce,
  Trouble in Pairs. Tahngarth, First Mate's "whenever an opponent attacks with one or more creatures, if Tahngarth is
  tapped" splits into the event condition and the intervening "if".
- "Whenever your commander attacks" from a graveyard (Endless Ranks of HYDRA, Vivi's Persistence) is
  `Triggers.creatureAttacks().withFilter(new PermanentIsCommanderPredicate(true))`, a per-creature trigger, where it
  was a "you attack" trigger with a `HasAttacker` intervening "if" that the collector fanned out by its class.

Idempotent.

Usage: step15-run17k-cards.py <letter module dir>...
"""
import pathlib
import re
import sys

EVENT_CARDS = set(("Archnemesis AureliaTheLawAbove CunningRhetoric CurseOfBounty CurseOfChaos CurseOfDisturbance "
                   "CurseOfInertia CurseOfOpulence CurseOfShallowGraves CurseOfVerbosity CurseOfVitality "
                   "EverettKRossHaplessAttache FiremaneCommando FlummoxedCyclops JoleneThePlunderQueen "
                   "MilaCraftyCompanion TenuousTruce TroubleInPairs").split())

OPPONENT_FAN_OUT = re.compile(
    r"Triggers\.playerAttacks\(\),\s*TriggerOptions\.DEFAULT\.withInterveningIf\(new PlayerAttacksOneOfYourOpponents\(\)\),")
KARAZIKAR = re.compile(
    r"Triggers\.playerAttacks\(\),\s*TriggerOptions\.DEFAULT\.withInterveningIf\(new OpponentAttacksAnotherOpponent\(\)\),")
KILLIAN = re.compile(
    r"Triggers\.playerAttacks\(\),\s*TriggerOptions\.DEFAULT\.withInterveningIf\(\s*"
    r"new MinimumMatchingAttackers\(1, new PermanentIsEnchantedBySourceControllerAuraPredicate\(\)\)\),")
TAHNGARTH = re.compile(
    r"TriggerOptions\.DEFAULT\.withInterveningIf\(\s*new AllOf\(List\.of\(\s*new AttackingPlayerIsOpponent\(\),\s*"
    r"new MinimumAttackers\(1\),\s*new SourceIsTapped\(\)\s*\)\)\),")
COMMANDER = re.compile(
    r"new TriggerCondition\(Triggers\.youAttack\(\), AbilityZones\.GRAVEYARD\),\s*"
    r"TriggerOptions\.DEFAULT\.withInterveningIf\(new HasAttacker\(new PermanentIsCommanderPredicate\(true\)\)\),")


def closing(source, index):
    depth = 0
    for j in range(index, len(source)):
        if source[j] == "(":
            depth += 1
        elif source[j] == ")":
            depth -= 1
            if depth == 0:
                return j
    raise ValueError("unbalanced")


def player_attacks_event_conditions(source):
    """Every intervening "if" of a `Triggers.playerAttacks()` statement becomes its event condition."""
    out, pos = [], 0
    for match in re.finditer(r"(addTrigger|new TriggeredAbility)\(", source):
        if match.start() < pos:
            continue
        end = closing(source, match.end() - 1) + 1
        statement = source[match.start():end]
        if re.search(r"Triggers\.playerAttacks\(\)", statement[:120]):
            statement = statement.replace(".withInterveningIf(", ".withEventCondition(")
        out.append(source[pos:match.start()])
        out.append(statement)
        pos = end
    out.append(source[pos:])
    return "".join(out)


def drop_unused_imports(source):
    imports = list(re.finditer(r"^import [\w.]+;\n", source, flags=re.M))
    body = source[imports[-1].end():] if imports else source
    def keep(match):
        name = match.group(1).rsplit(".", 1)[1]
        return match.group(0) if re.search(r"\b%s\b" % re.escape(name), body) else ""
    return re.sub(r"^import ((?:com\.github\.laxika\.magicalvibes|java\.util)\.[\w.]+);\n", keep, source, flags=re.M)


def convert(name, source):
    if name in ("CombatCalligrapher", "EllieBrickMaster"):
        source = OPPONENT_FAN_OUT.sub("Triggers.playerAttacksOneOfYourOpponents(),", source)
    elif name == "KarazikarTheEyeTyrant":
        source = KARAZIKAR.sub("Triggers.opponentAttacksAnotherOpponent(),", source)
    elif name in ("BreenaTheDemagogue", "SquallGunbladeDuelist"):
        source = source.replace("addTrigger(Triggers.playerAttacks(),",
                                "addTrigger(Triggers.playerAttacksOneOfYourOpponents(),")
        source = source.replace("PlayerAttacksOneOfYourOpponentsWithPowerOrToughnessEqualToSourceChosenNumber",
                                "AttackersHavePowerOrToughnessEqualToSourceChosenNumber")
    elif name == "KillianDecisiveMentor":
        source = KILLIAN.sub("Triggers.playerAttacks()\n                        .withFilter("
                             "new PermanentIsEnchantedBySourceControllerAuraPredicate()),", source)
    elif name == "TahngarthFirstMate":
        source = TAHNGARTH.sub("TriggerOptions.DEFAULT.withEventCondition(new AttackingPlayerIsOpponent())\n"
                               "                        .withInterveningIf(new SourceIsTapped()),", source)
    elif name in ("EndlessRanksOfHYDRA", "VivisPersistence"):
        source = COMMANDER.sub("new TriggerCondition(Triggers.creatureAttacks()\n"
                               "                                .withFilter(new PermanentIsCommanderPredicate(true)),\n"
                               "                        AbilityZones.GRAVEYARD),\n"
                               "                TriggerOptions.DEFAULT,", source)
    elif name in EVENT_CARDS:
        source = player_attacks_event_conditions(source)
    else:
        return source
    return drop_unused_imports(source)


def main():
    changed = []
    for module in sys.argv[1:]:
        for path in sorted(pathlib.Path(module).glob("src/main/java/**/*.java")):
            source = path.read_text()
            result = convert(path.stem, source)
            if result != source:
                path.write_text(result)
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
