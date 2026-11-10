#!/usr/bin/env python3
"""Step 15, run 17z14h: the cards that write one Oracle ability as several `addTrigger` calls on a trigger other than a
step's (row 17z14h; `step15-run17z14-scan.py`'s list), and the "blocks" abilities that didn't say how often they
trigger.

`SAGAS`: a chapter symbol is one chapter ability ("{rN}—[Effect]", CR 715.2b; "{rN1}, {rN2}—[Effect]" is one for each
numeral, CR 715.2c), so every effect of one chapter is one `addTrigger`. `Card.addTriggeredAbility` joined a card's
calls of one chapter into one ability at construction, so each was one stack object already; the cards now write what
they print. The merge is mechanical: the calls of one chapter in one block become the first call with every effect, in
the order they were written.

`BLOCKS_ONCE`: "whenever this creature blocks" and "attacks or blocks" trigger once each combat however many creatures
the creature blocks (CR 509.3a), which is `TriggerOptions.ONE_OR_MORE` on the ability; "blocks a creature" and
"blocks or becomes blocked by a creature" trigger for each creature (CR 509.3b; Ashmouth Hound's and Engulfing
Slagwurm's rulings) and stay as written. Each card was read against its Oracle text (Scryfall MCP).

Usage: step15-run17z14h-cards.py [LETTER...] (run per letter module; no letter means all). Fails if an edit didn't
apply; idempotent.
"""
import collections
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"

SAGAS = [
    "AgeOfUltron", "ArniSlaysTheTroll", "BahamutWardenOfLight", "BefriendingTheMoths", "BraskasFinalAeon",
    "ChainersTorment", "DayOfTheMoon", "DownDownToGoblinTown", "DownInTheValley", "EsperTerra",
    "HaraldUnitesTheElves", "HeavenSent", "IfritWardenOfInferno", "InTheDarknessBindThem", "InventiveIteration",
    "KangDynasty", "KingNarfisBetrayal", "KioraBestsTheSeaGod", "KumanoFacesKakkazan", "LeavesFromTheVine",
    "LoveSongOfNightAndDay", "NightmaresAndDaydreams", "NikoDefiesDestiny", "OathOfEorl", "OfHerbsAndStewedRabbit",
    "OkibaReckonerRaid", "OriginOfCaptainAmerica", "OriginOfIronMan", "OriginOfSpiderMan", "OriginOfTheHulk",
    "PhoenixWardenOfFire", "PhyrexianScriptures", "ShivaWardenOfIce", "SongOfERendil", "SongOfFreyalise",
    "SummonEsperMaduin", "SummonEsperValigarmanda", "SummonIxion", "SummonKnightsOfRound", "SummonKujata",
    "SummonPrimalGaruda", "SummonPrimalOdin", "SummonShiva", "SummonTitan", "SummonValefor",
    "TalesOfMasterSeshiro", "TeachingsOfTheKirin", "TheBindingOfTheTitans", "TheCrueltyOfGix", "TheCurseOfFenric",
    "TheDragonKamiReborn", "TheElderDragonWar", "TheEleventhHour", "TheHuntsmansRedemption",
    "TheMendingOfDominaria", "TheModernAge", "ThePrincessTakesFlight", "TheRavensWarning", "TheShatteredStatesEra",
    "TheSuperHeroCivilWar", "TheTricksterGodsHeist", "TheTriumphOfAnax", "TheTrueScriptures", "TheWarInHeaven",
    "TheWeatherseedTreaty", "ThreeBlindMice", "ThunderOfUnity", "TimeOfIce", "UrzaAssemblesTheTitans",
    "Vault112SadisticSimulation", "Vault13DwellersJourney", "Vault87ForcedEvolution", "WelcomeToJurassicPark",
    "WorldWarHulk",
]
# card -> [(old, new)], applied before the mechanical merge: the chapters written as a chain on a target
EDITS = {
    "InTheDarknessBindThem": [(
        """                .addTrigger(Triggers.chapter(4),
                        GainControlOfTargetEffect.withTargetPredicate(
                                ControlDuration.END_OF_TURN, target.predicate()))
                .addTrigger(Triggers.chapter(4),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addTrigger(Triggers.chapter(4),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addTrigger(Triggers.chapter(4), new RingTemptsYouEffect());""",
        """                .addTrigger(Triggers.chapter(4),
                        GainControlOfTargetEffect.withTargetPredicate(
                                ControlDuration.END_OF_TURN, target.predicate()),
                        new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET),
                        new RingTemptsYouEffect());""")],
    "TheShatteredStatesEra": [(
        """                .addTrigger(Triggers.chapter(1),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN))
                .addTrigger(Triggers.chapter(1),
                        new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.chapter(1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));""",
        """                .addTrigger(Triggers.chapter(1),
                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN),
                        new UntapPermanentsEffect(TapUntapScope.TARGET),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET));""")],
}

# "Whenever this creature blocks" / "attacks or blocks" (CR 509.3a), read against the Oracle text of each.
BLOCKS_ONCE = [
    "AgelessSentinels", "ArchitectOfRestoration", "AshcoatOfTheShadowSwarm", "AshiokNightmareMuse",
    "BebopRocksteady", "BrackwaterElemental", "BrunaLightOfAlabaster", "Brushwagg", "BurningSunCavalry",
    "CarrionRats", "CarrionWurm", "CeremonialGuard", "ClockworkBeetle", "ClockworkCondor", "ClockworkHydra",
    "Cunning", "DaemogothTitan", "DefiantVanguard", "DorotheaVengefulVictim", "Dwindle", "EmbalmedBrawler",
    "FogElemental", "GetawayCar", "GoblinPsychopath", "HamletCaptain", "HordeAmbusher", "Imprison",
    "KangeeSkyWarden", "LesserGargadon", "LibertyPrimeRecharged", "LoafingGiant", "LuminousWake",
    "ManaChargedDragon", "MarduBlazebringer", "MorkrutNecropod", "NascentMetamorph", "PhantomWhelp",
    "PrimalCocoon", "QuicksilverBehemoth", "RottingGiant", "RunawayCarriage", "SaprazzanOutrigger",
    "SlinkingGiant", "SmugglersCopter", "SpiderMobile", "StoicEphemera", "SuperSoldierSerum", "TakenumaBleeder",
    "TenuredOilcaster", "TimeElemental", "TreacherousVampire", "Vebulid", "VelukanDragon", "VeteransArmaments",
    "WallOfJunk", "WandOfOrcus", "WickerWarcrawler", "WildwoodTracker", "Windscouter",
]
# "Whenever this creature blocks a creature" / "blocks or becomes blocked by a creature" (CR 509.3b): for each
# creature blocked, as written. Listed so that an unfiltered "blocks" ability without a multiplicity is always a read
# card.
BLOCKS_EACH = {
    "AetherMembrane", "Aetherplasm", "AislingLeprechaun", "AlabornZealot", "AshmouthHound", "AssembledAlphas",
    "BrimazKingOfOreskos", "ClericOfChillDepths", "DeadIronSledge", "DreamFighter", "EngulfingSlagwurm",
    "FlailingDrake", "FlameheartWerewolf", "InfernalMedusa", "InfernoElemental", "KaijinOfTheVanishingTouch",
    "KessigForgemaster", "LabyrinthMinotaur", "LimDLsCohort", "LoyalSentry", "MammothHarness", "Meglonoth",
    "MesmerizingBenthid", "MindbenderSpores", "OrneryGoblin", "SawtoothOgre", "ShapeStealer",
    "ShieldOfTheRighteous", "SkewerSlinger", "TalruumChampion", "TangleAsp", "TreefolkMystic", "VenomousDragonfly",
    "VertigoSpawn", "WallOfCaltrops", "WallOfDust", "WallOfFrost", "WallOfTears", "WitherscaleWurm",
}
UNSAID_BLOCKS = re.compile(r"Triggers\.blocks\(\)(\))?,(?!\s*TriggerOptions\.)")
OPTIONS_IMPORT = "import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;\n"
TRIGGERS_IMPORT = "import com.github.laxika.magicalvibes.model.trigger.Triggers;\n"
CHAPTER_CALL = re.compile(r"^([ \t]*)addTrigger\((Triggers\.chapter\(\w+\)),", re.M)


def find(name):
    paths = list(ROOT.glob(f"*/{PKG}/*/{name}.java"))
    if len(paths) != 1:
        raise SystemExit(f"{name}: found {len(paths)} files")
    return paths[0].relative_to(ROOT).parts[0], paths[0]


def call_end(text, open_paren):
    """The index after the `);` closing the call whose `(` is at `open_paren`."""
    depth, quoted = 0, False
    for index in range(open_paren, len(text)):
        char = text[index]
        if quoted:
            quoted = not (char == '"' and text[index - 1] != "\\")
        elif char == '"':
            quoted = True
        elif char in "([{":
            depth += 1
        elif char in ")]}":
            depth -= 1
            if depth == 0:
                if text[index + 1] != ";":
                    raise SystemExit("a chapter call that isn't a statement")
                return index + 2
    raise SystemExit("an unclosed chapter call")


def arguments(text, start, end):
    """The top-level arguments of the call text[start:end] (between its parentheses), as (offset, source) pairs."""
    result, depth, quoted, begin = [], 0, False, start
    for index in range(start, end):
        char = text[index]
        if quoted:
            quoted = not (char == '"' and text[index - 1] != "\\")
        elif char == '"':
            quoted = True
        elif char in "([{":
            depth += 1
        elif char in ")]}":
            depth -= 1
        elif char == "," and depth == 0:
            result.append((begin, text[begin:index]))
            begin = index + 1
    result.append((begin, text[begin:end]))
    return result


def reindented(text, offset, source, statement_indent):
    """An effect argument as it reads on a continuation line of its own, its inner lines moved with it."""
    stripped = source.strip()
    first = offset + (len(source) - len(source.lstrip()))
    line_start = text.rfind("\n", 0, first) + 1
    line = text[line_start:first]
    logical = len(line) if not line.strip() else len(line) - len(line.lstrip())
    shift = len(statement_indent) + 8 - logical
    lines = stripped.split("\n")
    moved = [lines[0]]
    for inner in lines[1:]:
        if shift >= 0:
            moved.append(" " * shift + inner if inner.strip() else inner)
        else:
            moved.append(inner[-shift:] if inner[:-shift].strip() == "" else inner.lstrip())
    return "\n".join(moved)


def merge_chapters(name, text):
    """`text` with the `addTrigger` calls of one chapter in one block written as one call."""
    while True:
        calls = collections.defaultdict(list)
        for match in CHAPTER_CALL.finditer(text):
            calls[(match.group(1), match.group(2))].append(match)
        group = None
        for (indent, trigger), matches in calls.items():
            for first, second in zip(matches, matches[1:]):
                between = text[first.end():second.start()]
                # One block: nothing between the two calls closes a block opened before the first.
                if between.count("}") <= between.count("{") and not re.search(r"^\s*}", between, re.M):
                    group = (indent, trigger, first, second)
                    break
            if group:
                break
        if not group:
            return text
        indent, trigger, first, second = group
        first_end = call_end(text, first.end() - len(trigger) - 2)
        second_end = call_end(text, second.end() - len(trigger) - 2)
        effects = []
        for match, end in ((first, first_end), (second, second_end)):
            args = arguments(text, match.end() - len(trigger) - 1, end - 2)
            if args[0][1].strip() != trigger or len(args) < 2 or "TriggerOptions" in args[1][1]:
                raise SystemExit(f"{name}: a chapter call the merge doesn't read: {trigger}")
            effects += [reindented(text, offset, source, indent) for offset, source in args[1:]]
        one_line = f"{indent}addTrigger({trigger}, {', '.join(effects)});"
        if len(one_line) <= 120 and not any("\n" in effect for effect in effects):
            merged = one_line
        else:
            merged = f"{indent}addTrigger({trigger},\n" + ",\n".join(
                f"{indent}        {effect}" for effect in effects) + ");"
        # The second call's lines go, with the comment lines right above it, which move above the merged call.
        second_start = second.start()
        comments = []
        while True:
            previous_start = text.rfind("\n", 0, second_start - 1) + 1
            previous = text[previous_start:second_start]
            if previous.strip().startswith("//") and previous_start > first_end:
                comments.insert(0, previous)
                second_start = previous_start
            else:
                break
        tail_start = second_end + 1 if text[second_end] == "\n" else second_end
        middle = text[first_end:second_start]
        # A blank line that separated the removed call from what was above it goes with it.
        if middle.endswith("\n\n") and (text[tail_start:tail_start + 1] == "\n"
                                        or text[tail_start:].lstrip(" \t").startswith("}")
                                        or middle.strip() == ""):
            middle = middle[:-1]
        text = text[:first.start()] + "".join(comments) + merged + middle + text[tail_start:]


def remaining_chapter_splits(text):
    counts = collections.Counter(match.group(2) for match in re.finditer(
        r"(\.?)addTrigger\((Triggers\.chapter\(\w+\)),", text))
    return [trigger for trigger, count in counts.items() if count > 1]


def blocks_once(name, text):
    """`text` with its unfiltered "blocks" ability written once each combat, and whether it changed."""
    if name in ("BurningSunCavalry", "WildwoodTracker"):
        edited = text.replace("TriggerOptions.DEFAULT.with", "TriggerOptions.ONE_OR_MORE.with")
        if edited.count("Triggers.blocks()),\n                TriggerOptions.ONE_OR_MORE.with") != 1:
            raise SystemExit(f"{name}: blocks edit didn't apply")
        return edited, edited != text
    found = UNSAID_BLOCKS.findall(text)
    if not found:
        if len(re.findall(r"Triggers\.blocks\(\)\)?,\s*TriggerOptions\.ONE_OR_MORE", text)) != 1:
            raise SystemExit(f"{name}: blocks edit didn't apply")
        return text, False
    if len(found) != 1:
        raise SystemExit(f"{name}: {len(found)} blocks abilities")
    edited = UNSAID_BLOCKS.sub(lambda match: f"Triggers.blocks(){match.group(1) or ''}, TriggerOptions.ONE_OR_MORE,",
                               text)
    lines = edited.split("\n")
    for index, line in enumerate(lines):
        if len(line) > 120 and "TriggerOptions.ONE_OR_MORE, " in line:
            head, tail = line.split("TriggerOptions.ONE_OR_MORE, ", 1)
            indent = " " * (len(line) - len(line.lstrip()) + 8)
            lines[index] = f"{head}TriggerOptions.ONE_OR_MORE,\n{indent}{tail}"
    edited = "\n".join(lines)
    if OPTIONS_IMPORT not in edited:
        if edited.count(TRIGGERS_IMPORT) != 1:
            raise SystemExit(f"{name}: no place for the TriggerOptions import")
        edited = edited.replace(TRIGGERS_IMPORT, OPTIONS_IMPORT + TRIGGERS_IMPORT)
    return edited, True


def main():
    letters = set(sys.argv[1:])
    for name in SAGAS:
        module, path = find(name)
        if letters and module not in letters:
            continue
        original = text = path.read_text()
        for old, new in EDITS.get(name, []):
            if new in text:
                continue
            if text.count(old) != 1:
                raise SystemExit(f"{name}: edit didn't apply")
            text = text.replace(old, new)
        text = merge_chapters(name, text)
        if remaining_chapter_splits(text):
            raise SystemExit(f"{name}: chapters still split: {remaining_chapter_splits(text)}")
        if text != original:
            path.write_text(text)
            print(f"edited {module}/{name}")
    for name in BLOCKS_ONCE:
        module, path = find(name)
        if letters and module not in letters:
            continue
        text, changed = blocks_once(name, path.read_text())
        if changed:
            path.write_text(text)
            print(f"edited {module}/{name}")
    if not letters:
        for path in ROOT.glob(f"*/{PKG}/*/*.java"):
            if path.stem in BLOCKS_EACH or path.stem in BLOCKS_ONCE:
                continue
            code = "\n".join(line for line in path.read_text().split("\n")
                             if not line.strip().startswith(("//", "*", "/*")))
            if UNSAID_BLOCKS.search(code):
                raise SystemExit(f"{path.stem}: an unfiltered \"blocks\" ability this script doesn't list")


if __name__ == "__main__":
    main()
