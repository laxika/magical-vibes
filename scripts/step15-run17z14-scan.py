#!/usr/bin/env python3
"""Step 15, run 17z14: the cards with two or more triggered abilities on one trigger other than a step's (row 17z14h).

One Oracle ability is one `addTrigger` (CR 603.3): an ability split into several calls is several stack objects once
its reader reads each ability whole, and joined again only where a reader still merges abilities. Run 17z13e8c
converted the step triggers' cards and run 17z14 the planes' chaos abilities; this lists the rest, per card, with the
trigger written twice or more, to read against the Oracle text (many print separate abilities: evoke, myriad,
firebending, a Saga's "I, II" written per chapter is one ability per chapter).

Read-only. Usage: step15-run17z14-scan.py

`DONE` names the cards a run converted (its own `step15-run<RUN>-cards.py`), `SEPARATE` the cards a run read against
their Oracle text and kept apart because their abilities on that trigger really are separate; neither is listed again.
"""
import collections
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"

# Run 17z14b (scripts/step15-run17z14b-cards.py): one Oracle ability each, now one addTrigger.
DONE = {
    "Archipelagore", "SnapdaxApexOfTheHunt", "PolukranosWorldEater", "ShipbreakerKraken", "WildfireCerberus",
    "RoilingHorror", "SirenOfTheSilentSong", "ScouringSwarm", "Sharktocrab", "KrosDefenseContractor",
    "ObeliskSpider", "AuntieOolCursewretch", "ObNixilisUnshackled", "CoolButRude",
    # Run 17z14c (scripts/step15-run17z14c-cards.py): the self-cycled collector reads each ability whole.
    "YidaroWanderingMonster", "MangarasBlessing",
    # Run 17z14c2 (scripts/step15-run17z14c2-cards.py): the damage and becomes-the-target collectors read each ability
    # whole.
    "MyriadConstruct", "TeysaEnvoyOfGhosts", "RayneAcademyChancellor",
    # Run 17z14e (scripts/step15-run17z14e-cards.py): the attack collectors read each ability whole.
    "KarazikarTheEyeTyrant", "OathOfKaya", "TenuredOilcaster",
    # Run 17z14f (scripts/step15-run17z14f-cards.py): the block-step collectors read each ability whole.
    "KeeperOfTresserhorn", "LimDLsPaladin", "MasterOfCruelties",
    # Run 17z14g (scripts/step15-run17z14g-cards.py): the combat-damage collectors read each ability whole.
    "ArmMountedAnchor", "BumiUnleashed", "BusterSword", "NivMizzetGuildpact", "SwordOfBodyAndMind",
    "SwordOfFeastAndFamine", "SwordOfForgeAndFrontier", "SwordOfSinewAndSteel", "SwordOfWarAndPeace",
    "SwordOfWealthAndPower", "VengefulPharaoh",
    # Run 17z14h (scripts/step15-run17z14h-cards.py): a Saga's chapter is one chapter ability (CR 715.2b), so one
    # addTrigger of all its effects.
    "AgeOfUltron", "ArniSlaysTheTroll", "BahamutWardenOfLight", "BefriendingTheMoths", "BraskasFinalAeon",
    "ChainersTorment", "DayOfTheMoon", "DownDownToGoblinTown", "DownInTheValley", "EsperTerra",
    "HaraldUnitesTheElves", "HeavenSent", "IfritWardenOfInferno", "InTheDarknessBindThem", "InventiveIteration",
    "KangDynasty", "KingNarfisBetrayal", "KioraBestsTheSeaGod", "KumanoFacesKakkazan", "LeavesFromTheVine",
    "LoveSongOfNightAndDay", "NightmaresAndDaydreams", "NikoDefiesDestiny", "OathOfEorl", "OfHerbsAndStewedRabbit",
    "OkibaReckonerRaid", "OriginOfCaptainAmerica", "OriginOfIronMan", "OriginOfSpiderMan", "OriginOfTheHulk",
    "PhoenixWardenOfFire", "PhyrexianScriptures", "ShivaWardenOfIce", "SongOfERendil", "SongOfFreyalise",
    "SummonEsperMaduin", "SummonEsperValigarmanda", "SummonIxion", "SummonKnightsOfRound", "SummonKujata",
    "SummonPrimalGaruda", "SummonPrimalOdin", "SummonShiva", "SummonTitan", "SummonValefor", "TalesOfMasterSeshiro",
    "TeachingsOfTheKirin", "TheBindingOfTheTitans", "TheCrueltyOfGix", "TheCurseOfFenric", "TheDragonKamiReborn",
    "TheElderDragonWar", "TheEleventhHour", "TheHuntsmansRedemption", "TheMendingOfDominaria", "TheModernAge",
    "ThePrincessTakesFlight", "TheRavensWarning", "TheShatteredStatesEra", "TheSuperHeroCivilWar",
    "TheTricksterGodsHeist", "TheTriumphOfAnax", "TheTrueScriptures", "TheWarInHeaven", "TheWeatherseedTreaty",
    "ThreeBlindMice", "ThunderOfUnity", "TimeOfIce", "UrzaAssemblesTheTitans", "Vault112SadisticSimulation",
    "Vault13DwellersJourney", "Vault87ForcedEvolution", "WelcomeToJurassicPark", "WorldWarHulk",
}
# Run 17z14b: read against their Oracle text; their abilities on the listed trigger print apart (Class levels,
# vanishing beside its own ability, two cast abilities).
SEPARATE = {
    "AdvancedFloralInvocations", "GourmandsTalent", "RegenerationsRestored", "JeskaiAscendancy",
    # Run 17z14e: the cards with two or more abilities on one attack trigger, read against their Oracle text. Exalted
    # beside "whenever a creature you control attacks alone, ..." (CR 702.83a), firebending beside "whenever this
    # attacks, ...", "myriad, myriad" and two printed attack abilities are separate abilities. Catch of the Day's
    # three abilities and Typhoid Mary's two are written with event conditions that exclude each other (the choice
    # made as it entered; "if you discarded a card this turn, you choose one instead"), so one triggers at a time;
    # Lulu's two calls are two specialized faces' abilities, one for each face.
    "AngelicBenediction", "BattlegraceAngel", "FinestHour", "RafiqOfTheMany", "SovereignsOfLostAlara",
    "AzulaOnTheHunt", "AzulaRuthlessFirebender", "BoilingRockRioter", "ScurryOfSquirrels",
    "AureliaTheLawAbove", "ChampionsFromBeyond", "ReapersTalisman",
    "CatchOfTheDay", "TyphoidMaryFractured", "LuluForgetfulHollyphant",
    # Run 17z14f: No Quarter prints two abilities, "whenever a creature becomes blocked by a creature with lesser
    # power, destroy the blocking creature" and "whenever a creature blocks a creature with lesser power, destroy the
    # attacking creature".
    "NoQuarter",
    # Run 17z14g: Karai, Future of the Foot prints one ability ("return target creature card from your graveyard to
    # your hand. If her sneak cost was paid this turn, instead return that card to the battlefield"), written as two
    # whose event conditions exclude each other, so one triggers at a time and it is one object either way; merging
    # them needs one return effect whose destination is a condition, which no effect has.
    "KaraiFutureOfTheFoot",
    # Run 17z14h: evoke's "when this permanent enters, if its evoke cost was paid, its controller sacrifices it"
    # (CR 702.74a) beside the card's own enters ability or abilities (the five Incarnations of two "if {C}{C} was spent
    # to cast it" abilities print each on a line of its own).
    "Aethersnipe", "Briarhorn", "Catharsis", "Cloudthresher", "Dawnfluke", "Deceit", "Emptiness", "Endurance",
    "Faultgrinder", "FoundationBreaker", "Fury", "Glarewielder", "Grief", "Infestation", "IngotChewer",
    "InnerFlameAcolyte", "Mournwhelk", "Mulldrifter", "Shriekmaw", "Solitude", "SoulOfMigration", "Subtlety",
    "Vibrance", "Wavesifter", "Wispmare", "Wistfulness",
    # Run 17z14h: a keyword's enters or dies ability beside the card's own: offspring (CR 702.175a), squad
    # (CR 702.157a), ravenous (CR 702.156a), gift (CR 702.174b), For Mirrodin! (CR 702.163a), blitz's "when this
    # creature dies, draw a card" and haste-and-sacrifice (CR 702.152a), partner with, and "sacrifice it unless it
    # escaped" beside "whenever this enters or attacks".
    "BushyBodyguard", "FinchFormation", "FountainportCharmer", "IntrepidRabbit", "SplashLasher",
    "ThornplateIntimidator", "ThundertrapTrainer", "PowderGanger", "SicarianInfiltrator", "SpaceMarineDevastator",
    "WastelandRaider", "Broodlord", "Exocrine", "Mawloc", "Ravener", "Sporocyst", "Zoanthrope", "Kitnap",
    "Scrapshooter", "StarforgedSword", "HexgoldSledge", "GirderGoons", "NightClubber", "RiveteersProvocateur",
    "RiveteersRequisitioner", "WorkshopWarchief", "YannikScavengingSentinel", "KroxaTitanOfDeathsHunger",
    "UroTitanOfNaturesWrath",
    # Run 17z14h: "cascade, cascade" is an ability for each instance (CR 702.85a: "cascade" is one triggered
    # ability; Apex Devastator's reminder text: "multiple instances of cascade each trigger separately").
    "ApexDevastator", "CallForthTheTempest", "MaelstromWanderer",
    # Run 17z14h: two (Bladecoil Serpent: three) printed abilities, each "when this enters, ..." or "when this dies,
    # ..." on a line of its own: the Battlemages' and Archangel of Wrath's two kicked abilities, the Hedge-Mages' two
    # "if you control two or more ..." abilities, the "sacrifice it unless {C} was spent to cast it" creatures, the
    # "return a ... creature you control to its owner's hand" creatures, the "exile ... until this leaves the
    # battlefield" enchantments with a second enters ability, Linvala's two intervening "if"s, Nucklavee, Palace
    # Jailer, Relentless Dead, Sigardian Evangel.
    "AnaBattlemage", "NightscapeBattlemage", "StormscapeBattlemage", "SunscapeBattlemage", "ThornscapeBattlemage",
    "ThunderscapeBattlemage", "ArchangelOfWrath", "DuergarHedgeMage", "GwyllionHedgeMage", "HagHedgeMage",
    "NoggleHedgeMage", "SelkieHedgeMage", "AzoriusHerald", "CourtHussar", "CryptChampion", "PatagiaViper",
    "Plaxmanta", "SquealingDevil", "MarshCrocodile", "RazingSnidd", "SawtoothLoon", "Sparkcaster", "Stonecloaker",
    "CelebrateTheMountainKing", "LassoedByTheLaw", "PrisonRealm", "PullOfTheMistMoon", "StaticNet",
    "BladecoilSerpent", "LinvalaThePreserver", "Nucklavee", "PalaceJailer", "RelentlessDead", "SigardianEvangel",
    # Run 17z14h: not two abilities of one card on one trigger. Gale's five calls are one for each specialized face
    # (as Lulu's); Jessie Zane's second enters call is the ability the conjured Ambush Viper perpetually gains.
    "GaleConduitOfTheArcane", "JessieZaneFangbringer",
}
STEP = re.compile(r"Triggers\.(upkeep|drawStep|endStep|beginningOfCombat|precombatMainPhase|postcombatMainPhase"
                  r"|secondMainPhase|endOfCombat)\b")


def first_argument(text, start):
    """The first top-level argument of the call whose '(' is at `start`."""
    depth, quoted, current = 0, False, ""
    for index in range(start, len(text)):
        char = text[index]
        if quoted:
            current += char
            if char == '"' and text[index - 1] != "\\":
                quoted = False
            continue
        if char == '"':
            quoted = True
            current += char
        elif char in "([{":
            depth += 1
            if depth > 1:
                current += char
        elif char in ")]}":
            depth -= 1
            if depth == 0:
                return current.strip()
            current += char
        elif char == "," and depth == 1:
            return current.strip()
        else:
            current += char
    return current.strip()


def main():
    rows = []
    for path in sorted(ROOT.rglob("*.java")):
        text = path.read_text()
        counts = collections.Counter()
        for match in re.finditer(r"addTrigger\(", text):
            counts[re.sub(r"\s+", " ", first_argument(text, match.end() - 1))] += 1
        for trigger, count in counts.items():
            if count >= 2 and not STEP.search(trigger) and path.stem not in DONE | SEPARATE:
                rows.append((path.stem, count, trigger))
    for name, count, trigger in rows:
        print(f"{name} {count} {trigger}")
    print(f"{len(rows)} triggers, {len({row[0] for row in rows})} cards")


if __name__ == "__main__":
    main()
