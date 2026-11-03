#!/usr/bin/env python3
"""Step 14, run 4a of effect-slot-refactor.md, part 1: the "this [permanent] or another [type] you control enters" cards that
step 12b left split into a "when this enters" ability and an "another [type] you control enters" one become one
triggered ability with both trigger conditions, as run 3a merged its "this or another" cards (CR 603.3: one Oracle
ability is one stack object). The source's own condition is still collected by EtbTriggerService, the other one by its
entering watch, so neither entry is collected differently.

A card is merged when its constructor has exactly one `addTrigger(Triggers.enters(ObjectRelation.THIS), E)` and one
`addTrigger(Triggers.enters(X, ObjectRelation.ANOTHER_YOU_CONTROL), E)` with the same effect text E, either both
chained on one target group, the first on a target group and the second on the card, or both on the card. The merged
ability goes where the first was, on its target group. MANUAL is applied by exact replacement instead, for the cards
whose halves spell the effect differently or carry a third condition; --dry-run prints the diffs.

Usage: python3 scripts/step14-own-enters-merge.py [--dry-run] <module dir>...
"""
import difflib
import importlib.util
import os
import re
import sys

_spec = importlib.util.spec_from_file_location(
    "pilot_options", os.path.join(os.path.dirname(__file__), "step14-pilot-options.py"))
po = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(po)

# The cards whose Oracle text says "Whenever this [permanent] or another [type] you control enters" and whose halves
# have the same effect (scripts/step12b-self-or-another-scan.py lists the candidates with their wording).
MERGE = {
    "WhitewaterNaiads.java", "VeilOfAssimilation.java", "VerdantSunsAvatar.java", "UnderworldCoinsmith.java",
    "ThassasDevourer.java", "ThoughtrenderLamia.java", "TransplantTheorist.java", "Skybind.java",
    "StrengthFromTheFallen.java", "RadiantSolar.java", "PiousEvangel.java", "OakheartDryads.java",
    "OathOfTheAncientWood.java", "NyleasColossus.java", "KapshoKitefins.java", "HarvestguardAlseids.java",
    "HumblerOfMortals.java", "GoldenhideOx.java", "GrimGuardian.java", "ForgebornOreads.java",
    "EidolonOfBlossoms.java", "DaxossTorment.java", "DoomwakeGiant.java", "DowsingDevice.java",
    "DragonsparkReactor.java", "DreadbringerLampads.java", "CacophonyUnleashed.java", "CalixGuidedByFate.java",
    "ClementTheWorrywort.java", "BogwaterLumaret.java", "BoonOfTheSpiritRealm.java", "AgentOfErebos.java",
    # "Whenever this creature or another [restricted] creature you control enters" (or "another Zombie enters").
    "VerdantDread.java", "UneshCriosphinxSovereign.java", "TetzinGnomeChampion.java", "SageOfTheFalls.java",
    "ScourgeOfValkas.java", "RisenReef.java", "PossibilityTechnician.java", "NebelgastHerald.java",
    "NoxiousGhoul.java", "LanternScout.java", "LossarnachCaptain.java", "HawkeyeTrickShot.java",
    "HighPerfectMorcant.java", "HistoriansBoon.java", "GenghisFrog.java", "GoShintaiOfLifesOrigin.java",
    "GoblinAssassin.java", "DarksteelSplicer.java", "CoerciveRecruiter.java", "BalinLoremaster.java",
    "BeregondOfTheGuard.java", "BloomvineRegent.java", "AngelicSellSword.java", "AppaTheVigilant.java",
    "ArahboTheFirstFang.java", "ArbaazMir.java",
}

THIS = "Triggers.enters(ObjectRelation.THIS)"
OTHER = re.compile(r"^Triggers\.enters\(.*,\s*ObjectRelation\.(?:ANOTHER_YOU_CONTROL|ANOTHER)\)$", re.S)

MANUAL = {
    # "Whenever this creature or another artifact you control enters, put a +1/+1 counter on this creature. It can't
    # be blocked this turn": its own half was two abilities the enters collector pooled into one stack object.
    "KappaCannoneer.java": [(
        """        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect()));
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect())));""",
        """        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeBlockedEffect()))))));""")],
    # "Whenever this creature or another artifact you control enters, each opponent loses 1 life and you gain 1
    # life": the same.
    "PactdollTerror.java": [(
        """        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), SequenceEffect.of(
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(1)));""",
        """        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1))))));""")],
    # "Eerie — Whenever this creature or another enchantment you control enters and whenever you fully unlock a Room":
    # one ability with three trigger conditions.
    "FearOfSleepParalysis.java": [(
        """        target(TargetFilters.creature(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), eerie)
                .addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), eerie)
                .addTrigger(Triggers.youFullyUnlockRoom(), eerie);""",
        """        target(TargetFilters.creature(), 0, 1)
                .addTriggeredAbility(new TriggeredAbility(List.of(
                                new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                                new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT,
                                        ObjectRelation.ANOTHER_YOU_CONTROL)),
                                new TriggerCondition(Triggers.youFullyUnlockRoom())),
                        TriggerOptions.DEFAULT, List.of(new EffectRegistration(eerie))));""")],
    # "Whenever this land or another land you control enters, if you control seven or more lands with different names,
    # create a 2/2 black Zombie creature token": one ability with the intervening "if" (CR 603.4) as its option. The
    # landfall condition stays YOU_CONTROL (the landfall watch leaves the land's own entry out); the source's own
    # condition comes first, so its own entry matches that one, which the enters collector collects.
    "FieldOfTheDead.java": [(
        """        ConditionalEffect landfall = new ConditionalEffect(
                new ControlsDistinctPermanentNamesCount(7, new PermanentIsLandPredicate()),
                CreateTokenEffect.blackZombie(1));

        // Whenever this land enters, if you control seven or more lands with different names, create
        // a 2/2 black Zombie creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), landfall);

        // Whenever another land you control enters, use the same condition and token effect.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), landfall);""",
        """        // Whenever this land or another land you control enters, if you control seven or more lands with different
        // names, create a 2/2 black Zombie creature token.
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL))),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new ControlsDistinctPermanentNamesCount(7, new PermanentIsLandPredicate())),
                List.of(new EffectRegistration(CreateTokenEffect.blackZombie(1)))));""")],
}

# Its own "when this enters, if you cast it" ability stays apart (run 4b converts its intervening "if").
MANUAL["CacophonyUnleashed.java"] = [(
    """        addTrigger(Triggers.enters(ObjectRelation.THIS), animationEffect());
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), animationEffect());""",
    """        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(Triggers.ENCHANTMENT,
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(animationEffect()))));""")]
# "When this creature enters, if it entered from your graveyard or you cast it from your graveyard": one "when this
# enters" ability whose intervening "if" (CR 603.4) has both halves, where a cast half and a "when this enters from a
# graveyard" half stood.
MANUAL["ArchfiendsVessel.java"] = [(
    """        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new CastFromZone(Zone.GRAVEYARD, true),
                        new ExileSelfAndCreateTokenEffect(demon)));
        addTrigger(Triggers.entersFrom(Zone.GRAVEYARD, null, ObjectRelation.THIS),
                new ConditionalEffect(new EnteredFromZone(Zone.GRAVEYARD, true),
                        new ExileSelfAndCreateTokenEffect(demon)));""",
    """        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new AnyOf(List.of(
                        new EnteredFromZone(Zone.GRAVEYARD, true), new CastFromZone(Zone.GRAVEYARD, true)))),
                new ExileSelfAndCreateTokenEffect(demon));"""),
    ("import com.github.laxika.magicalvibes.model.condition.CastFromZone;\n",
     "import com.github.laxika.magicalvibes.model.condition.AnyOf;\n"
     "import com.github.laxika.magicalvibes.model.condition.CastFromZone;\n")]
# "When this creature enters, if {W}{W} was spent to cast it" and "if {R}{R}": intervening "if"s (CR 603.4). The
# enters collector checks a mana-spent condition as the ability triggers and not again, as the wrapper's
# atTriggerTime said.
MANUAL["Catharsis.java"] = [
    ("""        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, ConditionalEffect.atTriggerTime(
                new ColorSpentToCast(ManaColor.WHITE, 2),
                new CreateTokenEffect(
                        2, "Kithkin", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN))));""",
     """        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new ColorSpentToCast(ManaColor.WHITE, 2)),
                new CreateTokenEffect(
                        2, "Kithkin", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN)));"""),
    ("""        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, ConditionalEffect.atTriggerTime(
                new ColorSpentToCast(ManaColor.RED, 2),
                SequenceEffect.of(new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES))));""",
     """        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                TriggerOptions.DEFAULT.withInterveningIf(new ColorSpentToCast(ManaColor.RED, 2)),
                SequenceEffect.of(new BoostAllOwnCreaturesEffect(1, 1),
                        new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)));""")]
# "When Oasis of Renewal enters and whenever a land card leaves your graveyard, seek a land card. This ability triggers
# only once each turn." (and the same for nonland cards): two abilities of two trigger conditions each, whose limit
# covers both conditions, where four abilities shared two keyed wrappers.
MANUAL["OasisOfRenewal.java"] = [
    ("""        addTrigger(Triggers.enters(ObjectRelation.THIS), OncePerTurnTriggerEffect.keyed(
                new SeekLibraryEffect(1, land), "land"));
        addTrigger(Triggers.enters(ObjectRelation.THIS), OncePerTurnTriggerEffect.keyed(
                new SeekLibraryEffect(1, nonland), "nonland"));
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, OncePerTurnTriggerEffect.keyed(
                new TriggeringCardConditionalEffect(land, new SeekLibraryEffect(1, land)), "land"));
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, OncePerTurnTriggerEffect.keyed(
                new TriggeringCardConditionalEffect(nonland, new SeekLibraryEffect(1, nonland)), "nonland"));""",
     """        TriggerOptions onceEachTurn = TriggerOptions.ONE_OR_MORE.withLimit(TriggerLimit.ONCE_EACH_TURN);
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.leavesGraveyard(new PermanentIsLandPredicate(),
                                ObjectRelation.YOU_OWN))),
                onceEachTurn, List.of(new EffectRegistration(new SeekLibraryEffect(1, land)))));
        addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.leavesGraveyard(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()), ObjectRelation.YOU_OWN))),
                onceEachTurn, List.of(new EffectRegistration(new SeekLibraryEffect(1, nonland)))));"""),
    ("import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;\n",
     "import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;\n"
     "import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;\n"
     "import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;\n"),
    ("import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n",
     "import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;\n"
     "import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;\n")]

# Its halves were two target groups with the same filter; the one ability has one.
MANUAL["CoerciveRecruiter.java"] = [(
    """        target(TargetFilters.creature()).addTrigger(Triggers.enters(ObjectRelation.THIS), gainControl());
        target(TargetFilters.creature()).addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.PIRATE))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                gainControl());""",
    """        target(TargetFilters.creature()).addTriggeredAbility(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                        new TriggerCondition(Triggers.enters(new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentHasSubtypePredicate(CardSubtype.PIRATE))),
                                ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(gainControl()))));""")]

# Comments that described the two halves the merge made one ability.
AFTER = {
    "OathOfTheAncientWood.java": [(
        """        // +1/+1 counter on target creature. The self half rides the ETB slot (target chosen as the
        // enchantment is cast); the ally half is queued as an enters-trigger target choice.""",
        """        // +1/+1 counter on target creature.""")],
    "UneshCriosphinxSovereign.java": [(
        """        // and the other into your graveyard. Two slots: Unesh's own ETB, plus other Sphinxes.""",
        """        // and the other into your graveyard.""")],
    "LossarnachCaptain.java": [(
        """        // The self half is unconditional: it must still trigger when a type-replacing effect makes this creature a non-Human.""",
        """        // Its own entry triggers it whatever its types: a type-replacing effect may make this creature a non-Human.""")],
}

IMPORTS = {
    "List": "java.util.List",
    "TriggeredAbility": "com.github.laxika.magicalvibes.model.TriggeredAbility",
    "EffectRegistration": "com.github.laxika.magicalvibes.model.EffectRegistration",
    "TriggerCondition": "com.github.laxika.magicalvibes.model.trigger.TriggerCondition",
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger.TriggerOptions",
}


def matching(src, i):
    """The index just past the parenthesis that closes the one at src[i]."""
    depth = 0
    while True:
        c = src[i]
        if c == '"':
            i = src.index('"', i + 1)
        elif c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1


def split_args(text):
    args, depth, start, i = [], 0, 0, 0
    while i < len(text):
        c = text[i]
        if c == '"':
            i = text.index('"', i + 1)
        elif c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        elif c == "," and depth == 0:
            args.append(text[start:i])
            start = i + 1
        i += 1
    args.append(text[start:])
    return [a.strip() for a in args]


def statements(src):
    """Each statement of the card's constructor that adds a trigger: (start, end, prefix, calls), where prefix is the
    `target(...)` it is chained on or None and calls the (name, arguments) of its chained calls."""
    for m in re.finditer(r"^        (target\(|addTrigger\()", src, re.M):
        i = m.start(1)
        prefix = None
        if m.group(1) == "target(":
            end = matching(src, i + len("target"))
            prefix = src[i:end]
            i = end
        calls = []
        while True:
            j = i
            while src[j] in " \n.":
                j += 1
            call = re.match(r"(addTrigger|addTriggeredAbility)\(", src[j:])
            if not call:
                break
            end = matching(src, j + len(call.group(1)))
            calls.append((call.group(1), split_args(src[j + len(call.group(1)) + 1:end - 1])))
            i = end
        if not calls or src[i] != ";":
            continue
        yield m.start(), i + 2, prefix, calls


def norm(text):
    return re.sub(r"\s+", "", text)


def reindent(text, indent):
    lines = text.strip().split("\n")
    rest = [l for l in lines[1:] if l.strip()]
    shift = min((len(l) - len(l.lstrip()) for l in rest), default=0)
    return "\n".join([lines[0]] + [" " * indent + l[shift:] for l in lines[1:]])


def merged(prefix, this_trigger, other_trigger, effect):
    base = 8 if prefix is None else 16
    lead = "" if prefix is None else prefix + "\n" + " " * 16 + "."
    other = " " * (base + 16) + "new TriggerCondition(" + other_trigger + ")),"
    if len(other) > 120:
        filter_text, relation = other_trigger[len("Triggers.enters("):-1].rsplit(",", 1)
        other = (" " * (base + 16) + "new TriggerCondition(Triggers.enters(" + filter_text.strip() + ",\n"
                 + " " * (base + 24) + relation.strip() + "))),")
    options = " " * (base + 8) + "TriggerOptions.DEFAULT, List.of(new EffectRegistration("
    body = reindent(effect, base + 16)
    if len(options + body.split("\n")[0]) > 116:
        body = "\n" + " " * (base + 16) + reindent(effect, base + 24)
    return (lead + "addTriggeredAbility(new TriggeredAbility(List.of(\n"
            + " " * (base + 16) + "new TriggerCondition(" + this_trigger + "),\n"
            + other + "\n" + options + body + "))));")


def merge(src):
    found = list(statements(src))
    this = [(s, k) for s in found for k, (name, args) in enumerate(s[3])
            if name == "addTrigger" and len(args) == 2 and args[0] == THIS]
    other = [(s, k) for s in found for k, (name, args) in enumerate(s[3])
             if name == "addTrigger" and len(args) == 2 and OTHER.match(args[0])]
    if len(this) != 1 or len(other) != 1:
        raise ValueError("expected one own and one other enters ability, got %d and %d" % (len(this), len(other)))
    (s1, k1), (s2, k2) = this[0], other[0]
    effect = s1[3][k1][1][1]
    if norm(effect) != norm(s2[3][k2][1][1]):
        raise ValueError("the halves' effects differ")
    if s1 is s2:
        if len(s1[3]) != 2:
            raise ValueError("a chain with other calls")
    elif len(s1[3]) != 1 or len(s2[3]) != 1 or s2[2] is not None:
        raise ValueError("an unexpected shape")
    text = "        " + merged(s1[2], THIS, s2[3][k2][1][0], effect) + "\n"
    starts = sorted([(s1[0], s1[1]), (s2[0], s2[1])] if s1 is not s2 else [(s1[0], s1[1])], reverse=True)
    for start, end in starts:
        src = src[:start] + (text if start == s1[0] else "") + src[end:]
    return src


def main():
    dry = "--dry-run" in sys.argv
    for module in [a for a in sys.argv[1:] if not a.startswith("--")]:
        for root, _, files in os.walk(module):
            if "/build/" in root:
                continue
            for name in sorted(files):
                if name not in MERGE and name not in MANUAL:
                    continue
                path = os.path.join(root, name)
                original = open(path).read()
                src = original
                if name in MANUAL:
                    for old, new in MANUAL[name]:
                        if old not in src and new in src:
                            continue
                        if old not in src:
                            raise ValueError("%s: MANUAL text not found: %s" % (name, old[:80]))
                        src = src.replace(old, new, 1)
                elif "new TriggerCondition(Triggers.enters(ObjectRelation.THIS))" in src:
                    if not any(old in src for old, _ in AFTER.get(name, [])):
                        continue
                else:
                    try:
                        src = merge(src)
                    except ValueError as e:
                        print("SKIP %s: %s" % (name, e))
                        continue
                for old, new in AFTER.get(name, []):
                    src = src.replace(old, new, 1)
                src = po.add_imports(src, [IMPORTS[n] for n in sorted(IMPORTS)
                                           if re.search(r"\b" + n + r"\b", src)])
                src = po.drop_unused_imports(original, src)
                for line in src.split("\n"):
                    if len(line) > 120:
                        print("LONG %s: %s" % (name, line.strip()[:80]))
                if dry:
                    sys.stdout.writelines(difflib.unified_diff(original.splitlines(True), src.splitlines(True),
                                                               path, path))
                else:
                    open(path, "w").write(src)
                    print("merged " + name)


if __name__ == "__main__":
    sys.exit(main())
