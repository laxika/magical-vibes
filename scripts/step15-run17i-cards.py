#!/usr/bin/env python3
"""Step 15, run 17i: a condition of an attack trigger's event is its event condition, not an intervening "if".

CR 603.4: an intervening "if" is only an "if" that immediately follows the trigger condition. "Whenever this and at
least two other creatures attack", "whenever you attack with one or more Elves", "whenever this creature attacks while
saddled", "whenever enchanted player is attacked", "whenever this creature attacks a battle" name the trigger event
itself (CR 603.2), so the condition is the ability's event condition: judged as the ability triggers, with the attack
as the collector sees it, and not checked again as it resolves. So is a condition on which the ability exists (a
Class's level, a solved Case, a Siege's mode, an unlocked door).

For each card and condition in CONVERSIONS, the attack-trigger statement whose intervening "if" is that condition (a
`new Condition(...)` or a local variable bound to one) gets `withEventCondition` instead. The list is the Oracle scan's
(each ability's Oracle line has no ", if" after its trigger condition); Switchgrass Grazer is left out (its "if
saddled" belongs to part of its effect). Idempotent.

Usage: step15-run17i-cards.py <letter module dir>...
"""
import pathlib
import re
import sys

ROOT = "src/main/java/com/github/laxika/magicalvibes/cards"

CONVERSIONS = {
    "AllMatchingCreaturesAttack": "MobMentality",
    "AllOf": "FiremaneCommando IronsoulEnforcer",
    "AnyOf": "AinokStrikeLeader",
    "AttackedTargetIsMonarch": "TheSpearOfBashenga",
    "AttackedTargetIsOpponent": "KaaliaOfTheVast ShredderShadowMaster",
    "AttackedTargetMatches": "ThrashingFrontliner WarTrainedSlasher",
    "AttackingCreaturesTotalPowerAtLeast": "UginsMastery",
    "AttacksEnchantedPlayer": "CurseOfBounty CurseOfChaos CurseOfDisturbance CurseOfInertia CurseOfOpulence "
                              "CurseOfShallowGraves CurseOfVerbosity CurseOfVitality",
    "ControllerAndEnchantedPlayerAttackEachOther": "TenuousTruce",
    "ControlsPermanentCountAtMost": "KariZevCrewOfTwo",
    "ExactlyAttackers": "AlluringSuitor LoveOnTheBattlefield ValorsReach",
    "HasAttacker": "AkiriFearlessVoyager AkkiBattleSquad AlibouAncientWitness AloySaviorOfMeridian AmazingAlliance "
                   "AnimPakalThousandthMoon ArborealAlliance Archnemesis ClandestineMeddler DemonicCovenant "
                   "EndlessRanksOfHYDRA GlassCastHeart HiredClaw InterceptorShadowsHound JocastaAutomatonAvenger "
                   "JolenePlunderingPugilist KratosStoicFather MavrenFeinDuskApostle MetropolisAngel "
                   "NeyaliSunsVanguard RigoStreetwiseMentor SoaringThoughtThief TalionsMessenger TeoSpiritedGlider "
                   "VivisPersistence VulpineHarvester",
    "MinimumAttackers": "ArmasaurGuide ArthurMarigoldKnight AureliaTheLawAbove BomberCorps BorosElite BorosMastiff "
                        "CaptainAmericaSkybound ChampionsFromBeyond ChivalricAlliance CraigBooneNovacGuard "
                        "DaringSkyjek EiganjoDynastorianReplenish FalconJoaquinTorres FirefistStriker FiremaneAvenger "
                        "FiremaneCommando FrontlineMedic GalliaOfTheEndlessDance GeneralThunderboltRoss HaazdaMarshal "
                        "KarlovWatchdog LandrovalHorizonWitness LegionLoyalist LegionsLanding MakeshiftBattalion "
                        "MeddlingYouths MilitaryIntelligence NavSquadCommandos OdricMasterTactician OrdruunVeteran "
                        "OverwhelmingInstinct PaladinElizabethTaggerdy PhoenixChick RubyCollector SeasonedConsultant "
                        "SentinelSarahLyons SokkaLateralStrategist SontaranGeneral TajicBladeOfTheLegion WarcryPhoenix "
                        "WarmindInfantry WojekHalberdiers",
    "MinimumAttackingCreaturesOfSubtype": "HarbinVanguardAviator HermesOverseerOfElpis RaddicTalZealot "
                                          "SpinneretAndSpiderling",
    "MinimumMatchingAttackers": "AttumaAtlanteanWarlord BitterWork CelebornTheWise ChocoSeekerOfParadise "
                                "ChongAndLilyNomads CrimsonCowlMasterOfEvil DollmakersShopPorcelainGallery "
                                "GideonTheOathsworn GlimmerLens KillianDecisiveMentor MastersGuidance PathOfMettle "
                                "PoeticIngenuity ProdigysPrototype TheEarthKing TheUrDragon TideSkimmer VarinaLichQueen "
                                "WhirlwindKillerCyclone ZurzothChaosRider",
    "OpponentAttacksAnotherOpponent": "KarazikarTheEyeTyrant",
    "OpponentAttacksPlaneswalker": "MilaCraftyCompanion",
    "OpponentAttacksWithAtLeastCreatures": "Archnemesis CunningRhetoric EverettKRossHaplessAttache TroubleInPairs",
    "OpponentsAttackWithAtLeastCreatures": "FlummoxedCyclops",
    "PlayerAttacksOneOfYourOpponents": "CombatCalligrapher EllieBrickMaster",
    "PlayerAttacksOneOrMoreOfYourOpponents": "JoleneThePlunderQueen",
    "SourceClassLevelAtLeast": "GossipsTalent PartyDude",
    "SourceCounterThreshold": "FighterClass PaladinClass RangerClass",
    "SourceHasChosenMode": "CatchOfTheDay HollowmurkSiege",
    "SourceIsSaddled": "AlacrianJaguar AutarchMammoth BoundingFelidar BridledBighorn BrightfieldGlider "
                       "BrightfieldMustang BulwarkOx CalamityGallopingInferno CongregationGryff DistrictMascot "
                       "DracosaurAuxiliary DroverGrizzly FortuneLoyalSteed GiantBeaver GilaCourser GildedGhoda "
                       "GloryheathLynx GuardianSunmare LagorinSoulOfAlacria OrneryTumblewagg QuilledCharger "
                       "RamblingPossum RoutewayMoose SeraphicSteed TrainedArynx UnswervingSloth VenomsacLagac",
    "SourceIsSolved": "CaseOfTheMarketMelee CaseOfTheTrampledGarden",
    "SourceRoomDoorUnlocked": "PaintersStudioDefacedGallery",
}

# This run takes the self-attack family ("whenever this creature attacks"), whose collector judges each ability's
# event condition with the attacker, the number of attackers and the attack target. The "attacks and isn't blocked",
# "you attack", "a player attacks" and "a creature you control attacks" families are later runs.
ATTACK_FACTORIES = ("attacks",)
TRIGGER = re.compile(r"Triggers\.(" + "|".join(ATTACK_FACTORIES) + r")\(")


def closing(source, open_index):
    depth = 0
    for index in range(open_index, len(source)):
        if source[index] == "(":
            depth += 1
        elif source[index] == ")":
            depth -= 1
            if depth == 0:
                return index
    raise ValueError("unbalanced parentheses")


def condition_class(argument, variables):
    argument = argument.strip()
    match = re.match(r"new (\w+)\(", argument)
    if match:
        return match.group(1)
    return variables.get(argument)


def convert(source, conditions, name):
    variables = {m.group(1): m.group(2) for m in re.finditer(r"\b(\w+)\s*=\s*new (\w+)\(", source)}
    out, index, converted = [], 0, 0
    for match in re.finditer(r"(addTrigger|new TriggeredAbility)\(", source):
        if match.start() < index:
            continue
        end = closing(source, match.end() - 1)
        statement = source[match.start():end + 1]
        if TRIGGER.search(statement):
            def swap(option):
                nonlocal converted
                argument_end = closing(statement, option.end() - 1)
                if condition_class(statement[option.end():argument_end], variables) in conditions:
                    converted += 1
                    return "withEventCondition("
                return option.group(0)
            statement = re.sub(r"withInterveningIf\(", swap, statement)
        out.append(source[index:match.start()])
        out.append(statement)
        index = end + 1
    out.append(source[index:])
    return "".join(out), converted


def main(modules):
    by_card = {}
    for condition, cards in CONVERSIONS.items():
        for card in cards.split():
            by_card.setdefault(card, set()).add(condition)
    found = set()
    total = 0
    for module in modules:
        for path in sorted((pathlib.Path(module) / ROOT).glob("*/*.java")):
            if path.stem not in by_card:
                continue
            found.add(path.stem)
            source = path.read_text()
            new_source, converted = convert(source, by_card[path.stem], path.stem)
            if convert(new_source, by_card[path.stem], path.stem)[1]:
                raise SystemExit(f"{path.stem}: a listed intervening if was left in an attack trigger")
            if converted:
                path.write_text(new_source)
                total += converted
                print(f"{path.stem}: {converted}")
    print(f"{total} abilities converted")


if __name__ == "__main__":
    main(sys.argv[1:])
