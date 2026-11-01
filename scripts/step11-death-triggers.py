#!/usr/bin/env python3
"""Moves card classes off the creature-death EffectSlots (effect-slot-refactor.md step 11).

Every registration in one of the slots below becomes a triggered ability with a real trigger: a dies trigger with
the relation its Oracle wording gives (the classification step11-death-classify.py writes, corrected by RELATION),
and a filter for the kind of object. The rest follows the old collectors and the Oracle text:

* the "when this dies" registration a card added only so that its own death counted ("this creature or another",
  "a creature you control") is dropped, the ability now includes its source (DUPLICATE);
* consecutive registrations the old collector put on the stack as one object become one ability (MERGE);
* "this or another [restricted] creature", whose restriction is still the effect's conditional wrapper, stays a
  "when this dies" ability plus an "another" one, so the restriction never applies to the source (RELATION);
* grants (GrantTriggeredAbilityEffect, RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect) take the ability.

Cards in MANUAL are left alone and converted by hand. Usage, per letter module or for all of them:
    python3 scripts/step11-death-triggers.py classification.tsv magical-vibes-card/a [...]
"""
import csv
import os
import re
import sys

SLOT_KIND = {
    "ON_ALLY_CREATURE_DIES": "creature", "ON_ANY_CREATURE_DIES": "creature",
    "ON_OPPONENT_CREATURE_DIES": "creature", "ON_EQUIPPED_CREATURE_DIES": "creature",
    "ON_DAMAGED_CREATURE_DIES": "creature", "GRAVEYARD_ON_ALLY_CREATURE_DIES": "creature",
    "GRAVEYARD_ON_OPPONENT_CREATURE_DIES": "creature",
    "ON_ALLY_NONTOKEN_CREATURE_DIES": "nontoken", "ON_ANY_NONTOKEN_CREATURE_DIES": "nontoken",
    "ON_ENCHANTED_PLAYER_NONTOKEN_CREATURE_DIES": "nontoken",
    "ON_ALLY_CREATURE_OR_PLANESWALKER_DIES": "creatureOrPlaneswalker",
    "ON_HAUNTED_CREATURE_DIES": "any",
}
GRAVEYARD_SLOTS = {"GRAVEYARD_ON_ALLY_CREATURE_DIES", "GRAVEYARD_ON_OPPONENT_CREATURE_DIES"}
SLOT_RE = "|".join(SLOT_KIND)

# Wording the classifier misreads: the "other" or "or" belongs to something else on the card.
RELATION = {
    "Jerren": "ANOTHER_YOU_CONTROL",
    "OneWithTheKami": "YOU_CONTROL",
    "RampageOfTheValkyries": "YOU_CONTROL",
    "PucasCovenant": "YOU_CONTROL",
    "GravePact": "YOU_CONTROL",
    "Lifeline": "ANY",
    "GenestealerPatriarch": "ANY",
    "Purgatory": "ANY",
    "NimDeathmantle": "ANY",
    "DiabolicServitude": "ANY",
    "CarthTheLion": "YOU_CONTROL",
    "BaneclawMarauder": "ANY",
    "WaywardDisciple": "THIS_OR_ANOTHER_YOU_CONTROL",
    "EmbroseDeanOfShadow": "YOU_CONTROL",
    "CacklingCulprit": "THIS_OR_ANOTHER_YOU_CONTROL",
    # "This creature or another creature with [last-known power, mana value, supertype, color, keyword]": the
    # restriction needs a last-known value the trigger's filter can't read from the departed copy yet, so "this
    # creature" stays its own "when this dies" ability and "another" keeps the effect's wrapper (step 14 turns the
    # wrapper into the filter). Susurian Voidborn and Al Bhed Salvagers' "creature or artifact" also needs the
    # source's own death counted once whatever it is.
    "MycoidShepherd": "ANOTHER_YOU_CONTROL",
    "CaldaiaGuardian": "ANOTHER_YOU_CONTROL",
    "HaviTheAllFather": "ANOTHER_YOU_CONTROL",
    "AzlaskTheSwellingScourge": "ANOTHER_YOU_CONTROL",
    "ShadowTheHedgehog": "ANOTHER_YOU_CONTROL",
    "JackdawSavior": "ANOTHER_YOU_CONTROL",
    "SusurianVoidborn": "ANOTHER_YOU_CONTROL",
    "AlBhedSalvagers": "ANOTHER_YOU_CONTROL",
    # "This creature or another [subtype, type, nontoken] creature": the restriction is the effect's conditional
    # wrapper, which would reject the source itself when it no longer has the characteristic; split the same way.
    "ArchghoulOfThraben": "ANOTHER_YOU_CONTROL",
    "HeadlessRider": "ANOTHER_YOU_CONTROL",
    "KalastriaHighborn": "ANOTHER_YOU_CONTROL",
    "MarshlandHordemaster": "ANOTHER_YOU_CONTROL",
    "MiaraThornOfTheGlade": "ANOTHER_YOU_CONTROL",
    "MidnightEntourage": "ANOTHER_YOU_CONTROL",
    "MillicentRestlessRevenant": "ANOTHER_YOU_CONTROL",
    "OmnathLocusOfRage": "ANOTHER_YOU_CONTROL",
    "OrahSkyclaveHierophant": "ANOTHER_YOU_CONTROL",
    "PashalikMons": "ANOTHER_YOU_CONTROL",
    "RundveltHordemaster": "ANOTHER_YOU_CONTROL",
    "ScionOfOpulence": "ANOTHER_YOU_CONTROL",
    "UndeadAugur": "ANOTHER_YOU_CONTROL",
    "XathridNecromancer": "ANOTHER_YOU_CONTROL",
    "DragonKamisEgg": "ANOTHER_YOU_CONTROL",
    "WeddingCrasher": "ANOTHER_YOU_CONTROL",
    "PolukranosEngineOfRuin": "ANOTHER_YOU_CONTROL",
    "TheGhoulGunslinger": "ANOTHER_YOU_CONTROL",
    "AnaxHardenedInTheForge": "ANOTHER_YOU_CONTROL",
    "NickValentinePrivateEye": "ANOTHER_YOU_CONTROL",
    "RotlungReanimator": "ANOTHER",
    "SkirkDrillSergeant": "ANOTHER",
    "VengefulDead": "ANOTHER",
    "DrossScorpion": "ANOTHER",
}

# Cards whose "when this dies" registration only duplicated the slot ability for the source's own death; the value
# is which of the card's top-level "Triggers.dies(ObjectRelation.THIS)" registrations it is.
DUPLICATE = {name: 0 for name in """
ZulaportCutthroat WarteyeWitch VeneratedStormsinger VengefulBloodwitch VoldarenBloodcaster
TerrorsOfTheTrack TotentanzSwarmPiper SelhoffOccultist SlurrkAllIngesting
PanickedBystander PawnOfUlamog OdricsOutrider
MinotaurRoxxonCEO CordialVampire CruelCelebrant FalkenrathNoble
BasrisLieutenant BloodArtist ButcherOfMalakir
CassHandOfVengeance WaywardDisciple EmbroseDeanOfShadow CacklingCulprit
AkkiEmberKeeper AlharuSolemnRitualist AresGodOfWar AtlaPalaniNestTender BeifongsBountyHunters BishopOfWings
CleopatraExiledPharaoh CrosswayTroublemakers DawnEvangel DeathTyrant DeathsOasis DreadTiller Dreadhound DrossHarvester
EmbalmedAscendant FelisaFangOfSilverquill GladehartCavalry GristleGrinner GyoxBrutalCarnivora HomicideInvestigator
IndebtedSamurai JasonBrightGlowingProphet JenovaAncientCalamity JudithTheScourgeDiva KardurDoomscourge KheruBloodsucker
KratosStoicFather LazotepSliver LocthwainLancer Lumberknot LuminousBroodmoth MarchesaTheBlackRose MeltstriderEulogist
MerchantOfTruth MidnightReaper MinnWilyIllusionist PhenomenonInvestigators PhyrexianVivisector RampartArchitect
RaybladeTrooper ReyhanLastOfTheAbzan ShadowUrchin SkyclaveShadowcat Slimefoot Soulcatcher SpeciesSpecialist Sporogenesis
StoneHavenOutfitter SultaiFlayer TheMasterGallifreysEnd TheScorpionGod TheSwarmlord ThievingAmalgam TitaniaNaturesForce
TributaryInstructor UlvenwaldObserver ZahurGlorysPast
""".split()}

# Consecutive plain registrations the old collector put on the stack as one object.
MERGE = set("""
VindictiveVampire NornsWellspring MoldervineReclamation HeirloomAuntie DarkProphecy CauldronOfEssence BastionOfRemembrance
SilumgarScavenger SeizeTheSoul
""".split())

# Converted by hand: "one or more" batches, abilities in several zones, chained target groups, planes, delayed
# triggers and abilities whose source and other objects differ in what they do.
MANUAL = set("""
VengefulTownsfolk SpectacleOfDestruction SoulShredder SeerOfStolenSight RinoaAngelWing GreatFierceBee Chainsaw
BloodSpatterAnalysis DesperateMeasures Agyrem GrandOssuary KamiOfMourning ArnynDeathbloomBotanist VeinRipper
HostOfTheHereafter ReluctantRoleModel GenestealerPatriarch
Lifeline
""".split())


def find_close(src, open_index):
    """Index of the parenthesis closing the one at open_index, skipping strings and comments."""
    depth = 0
    i = open_index
    in_str = None
    while i < len(src):
        c = src[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in "\"'":
            in_str = c
        elif src.startswith("//", i):
            i = src.index("\n", i)
            continue
        elif c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError("unbalanced")


def split_args(text):
    """Splits a call's argument text at its top-level commas."""
    args, depth, start, in_str, i = [], 0, 0, None, 0
    while i < len(text):
        c = text[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in "\"'":
            in_str = c
        elif c in "(<{[":
            depth += 1
        elif c in ")>}]":
            depth -= 1
        elif c == "," and depth == 0:
            args.append(text[start:i].strip())
            start = i + 1
        i += 1
    args.append(text[start:].strip())
    return args


def trigger_expr(slot, relation):
    kind = SLOT_KIND[slot]
    rel = "ObjectRelation.%s" % relation
    if kind == "creature":
        return "Triggers.creatureDies(%s)" % rel
    if kind == "nontoken":
        return "Triggers.nontokenCreatureDies(%s)" % rel
    if kind == "creatureOrPlaneswalker":
        return "Triggers.dies(Triggers.CREATURE_OR_PLANESWALKER, %s)" % rel
    return "Triggers.dies(%s)" % rel


def remove_duplicate(src, index):
    """Drops the index-th top-level "addTrigger(Triggers.dies(ObjectRelation.THIS), ...);" statement and the comment
    lines right above it that explained the duplicate."""
    pattern = re.compile(r"^([ \t]*)(\w+\.)?addTrigger\(Triggers\.dies\(ObjectRelation\.THIS\)", re.M)
    matches = list(pattern.finditer(src))
    m = matches[index]
    close = find_close(src, src.index("(", m.start()))
    end = src.index(";", close) + 1
    if src[end:end + 1] == "\n":
        end += 1
    start = m.start()
    while True:
        prev = src.rfind("\n", 0, start - 1) + 1
        line = src[prev:start].strip()
        if line.startswith("//"):
            start = prev
            continue
        break
    return src[:start] + src[end:]


def convert_calls(src, card, relations):
    """Rewrites addEffect(EffectSlot.<slot>, ...) calls and the slot-taking grant constructors, keeping the text of
    their other arguments as it was."""
    out, pos = [], 0
    call = re.compile(r"(addEffect|new GrantTriggeredAbilityEffect|new RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect)"
                      r"\((\s*)EffectSlot\.(%s)\s*,(\s*)" % SLOT_RE)
    for m in call.finditer(src):
        if m.start() < pos:
            continue
        close = find_close(src, src.index("(", m.start()))
        slot = m.group(3)
        lead, gap = m.group(2), m.group(4)
        sep = "," + gap if "\n" in gap else ", "
        rest = src[m.end():close]
        relation = relations[slot]
        trigger = trigger_expr(slot, relation)
        head = m.group(1)
        if head == "addEffect":
            if slot in GRAVEYARD_SLOTS:
                body = "addTrigger(new TriggerCondition(%s, AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT%s%s)" % (
                    trigger, sep, rest)
            else:
                body = "addTrigger(%s%s%s)" % (trigger, sep, rest)
        else:
            args = split_args(rest)
            first = args[0]
            first_end = rest.index(first) + len(first)
            body = "%s(%snew TriggeredAbility(%s%s%s)%s)" % (head, lead, trigger, sep, first, rest[first_end:])
        out.append(src[pos:m.start()])
        out.append(body)
        pos = close + 1
    out.append(src[pos:])
    return wrap_long_lines("".join(out))


def wrap_long_lines(src):
    """Breaks a converted line longer than 120 characters after its trigger argument."""
    lines = []
    for line in src.split("\n"):
        m = re.match(r"^(\s*)(.*?(?:addTrigger|new TriggeredAbility)\((?:new TriggerCondition\()?Triggers\.[^;]*?\)(?:, AbilityZones\.GRAVEYARD\), TriggerOptions\.DEFAULT)?,) (.+)$", line)
        if len(line) > 120 and m:
            lines.append(m.group(1) + m.group(2))
            lines.append(m.group(1) + "        " + m.group(3))
        else:
            lines.append(line)
    return "\n".join(lines)


def merge_pairs(src, slot):
    """Simpler merge: the two registrations of slot become one with both effects, at the first one's place."""
    pattern = re.compile(r"(\.?)addEffect\(\s*EffectSlot\.%s\s*," % slot)
    matches = list(pattern.finditer(src))
    if len(matches) != 2:
        raise ValueError("merge needs exactly two registrations of " + slot)
    first, second = matches
    close1 = find_close(src, src.index("(", first.start()))
    close2 = find_close(src, src.index("(", second.start()))
    args2 = src[second.end():close2].strip()
    second_start = second.start()
    if first.group(1) == ".":
        # A chained call: ".addEffect(...)" directly follows the first one.
        second_end = close2 + 1
    else:
        line_start = src.rfind("\n", 0, second_start) + 1
        second_start = line_start
        second_end = src.index(";", close2) + 1
        if src[second_end:second_end + 1] == "\n":
            second_end += 1
    src = src[:second_start] + src[second_end:]
    return src[:close1] + ",\n                " + args2 + src[close1:]


IMPORTS = {
    "Triggers.": "com.github.laxika.magicalvibes.model.trigger.Triggers",
    "ObjectRelation.": "com.github.laxika.magicalvibes.model.trigger.ObjectRelation",
    "new TriggerCondition(": "com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
    "TriggerOptions.": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
    "AbilityZones.": "com.github.laxika.magicalvibes.model.AbilityZones",
    "new TriggeredAbility(": "com.github.laxika.magicalvibes.model.TriggeredAbility",
    "new PermanentHasSubtypePredicate(": "com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate",
}


def fix_imports(src):
    for marker, fqcn in IMPORTS.items():
        simple = fqcn.rsplit(".", 1)[1]
        if marker in src and not re.search(r"^import %s;$" % re.escape(fqcn), src, re.M) \
                and not re.search(r"^import [\w.]+\.%s;$" % simple, src, re.M):
            src = add_import(src, fqcn)
    body = re.sub(r"^import .*$", "", src, flags=re.M)
    if "EffectSlot" not in body:
        src = re.sub(r"^import com\.github\.laxika\.magicalvibes\.model\.EffectSlot;\n", "", src, flags=re.M)
    for fqcn in ("com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect",
                 "com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate"):
        simple = fqcn.rsplit(".", 1)[1]
        if simple not in re.sub(r"^import .*$", "", src, flags=re.M):
            src = re.sub(r"^import %s;\n" % re.escape(fqcn), "", src, flags=re.M)
    return src


def add_import(src, fqcn):
    """Adds an import in sorted position within its group (java.* or the project's)."""
    line = "import %s;" % fqcn
    is_java = fqcn.startswith("java")
    imports = [m for m in re.finditer(r"^import [\w.]+;$", src, re.M)
               if m.group(0).startswith("import java") == is_java]
    if not imports:
        last = list(re.finditer(r"^import [\w.]+;$", src, re.M))[-1]
        return src[:last.end()] + "\n\n" + line + src[last.end():]
    for m in imports:
        if m.group(0) > line:
            return src[:m.start()] + line + "\n" + src[m.start():]
    return src[:imports[-1].end()] + "\n" + line + src[imports[-1].end():]


def main():
    classification = {}
    for card, slot, relation, _ in csv.reader(open(sys.argv[1]), delimiter="\t"):
        classification.setdefault(card, {})[slot] = RELATION.get(card, relation)
    for root_dir in sys.argv[2:]:
        for root, _, files in os.walk(root_dir):
            for name in sorted(files):
                card = name[:-5]
                if not name.endswith(".java") or card not in classification or card in MANUAL:
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if not re.search(r"EffectSlot\.(%s)\b" % SLOT_RE, src):
                    continue
                relations = classification[card]
                if "?" in relations.values():
                    print("unclassified, skipped:", path)
                    continue
                if card in DUPLICATE:
                    src = remove_duplicate(src, DUPLICATE[card])
                if card in MERGE:
                    slot = next(iter(relations))
                    src = merge_pairs(src, slot)
                src = convert_calls(src, card, relations)
                src = fix_imports(src)
                open(path, "w").write(src)
                print("converted:", path)


if __name__ == "__main__":
    main()
