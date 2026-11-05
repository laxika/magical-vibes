#!/usr/bin/env python3
"""Takes the interveningIf / triggerTimeOnly flags out of the ConditionalEffects cards write
(effect-slot-refactor.md step 14, run 16s). A ConditionalEffect is now only an "if" checked as the effect
resolves; the intervening "if" (CR 603.4) and the event condition (CR 603.2) of a triggered ability are its
TriggerOptions.

- ConditionalEffect.unless(C, E) becomes new ConditionalEffect(C, E) (in card classes and card tests).
- new ConditionalEffect(C, E, true|false) becomes new ConditionalEffect(C, E) (card classes).
- "Attacks alone" (exalted, CR 702.83a) becomes the event condition it is, where it was an intervening "if".
- The abilities in OPTIONS, whose top-level effects are all ConditionalEffects of one condition, take that
  condition as their intervening "if" (IIF: "When/Whenever/At [event], if [condition], [effect]") or as their
  event condition (EV: a condition of the trigger event, never checked again as the ability resolves), and
  lose the wrapper. Every other ConditionalEffect in a triggered ability is an "if" of its effect (the Oracle
  text says so: "draw a card if ...", "Then if ...", "If ..., instead ..."), checked as it resolves.

Tunnel Ignus, Confounding Conundrum, Archaeomancer's Map and Invader Parasite are left as they are: the
"whenever a land an opponent controls enters" collector judges their conditions by type against the entering land
and its controller, which the trigger pipeline's context doesn't give (step 15); Edgar Markov's eminence, as the
command-zone cast reader takes no options (its zone already says it is in the command zone); Desert Were-Worm too: the "whenever
you attack" collector counts its "for the first time each turn" through the power condition around its
OncePerTurnTriggerEffect.

Running it again changes nothing.

Usage: python3 scripts/step14-intervening-if-flags.py
"""
import os
import re
import sys

ABILITY = re.compile(r"\baddTrigger\(|\baddSimultaneousTrigger\(|new TriggeredAbility\(")

# file name -> [(condition source, IIF or EV)]; the condition is matched with its whitespace collapsed.
OPTIONS = {
    "TrailOfMystery.java": [("new TargetPermanentMatches(new PermanentIsCreaturePredicate())", "IIF")],
    "PersistentMarshstalker.java": [("new GraveyardCardThreshold(7, null)", "IIF")],
    "ProgenitorMimic.java": [("new NotCondition(new SourceIsToken())", "IIF")],
    "ClockworkDragon.java": [("new SourceAttackedOrBlockedThisCombat()", "IIF")],
    "ClockworkVorrac.java": [("new SourceAttackedOrBlockedThisCombat()", "IIF")],
    "BridgeFromBelow.java": [("new SourceCardInGraveyard()", "IIF")],
    "AshiokWickedManipulator.java": [("new CardPutIntoExileThisTurn()", "IIF")],
    "FinestHour.java": [("new FirstCombatPhase()", "IIF")],
    "WeldfastWingsmith.java": [("new SourceMatchesPermanentPredicate(new PermanentIsArtifactPredicate())", "EV")],
    "ThroatWolf.java": [("new AllConditions(List.of(new FirstCombatPhase(), new NotControllerTurn()))", "EV")],
    "TwentyToedToad.java": [("new MinimumAttackers(2)", "EV")],
    "SeiferBalambRival.java": [("new BlockedByMinCreatures(2)", "EV")],
    "SnarlfangVermin.java": [("new SourceCardInGraveyard()", "EV")],
    "PathOfDiscovery.java": [("new SourceIsCreature()", "EV")],
    "PhoenixOfIteration.java": [("new SourceCardInGraveyard()", "IIF")],
    "KjeldoranFrostbeast.java": [("new SourceAttackedOrBlockedThisCombat()", "EV")],
    "KozileksReturn.java": [("new SourceCardInGraveyard()", "IIF")],
    "CarnivalOfSouls.java": [("new SourceIsCreature()", "EV")],
    "AetherFlash.java": [("new SourceIsCreature()", "EV")],
}

# An option the ability already has that is really part of its trigger event ("attacks alone", "with one or more
# Rats"): it becomes the event condition, so the "if" can be the intervening "if".
EXISTING_TO_EVENT = {
    "FinestHour.java": "new AttacksAlone()",
    "PersistentMarshstalker.java": "new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.RAT))",
}


# Abilities written by hand: an intervening "if" written inside the "you may" (Firemane Angel, Gatewatch Beacon, Ian
# the Reckless: "…, if [condition], you may …") or through a local variable (Bloodhall Priest, Naomi, Omnivorous
# Flytrap, Carpet of Flowers), one Oracle ability written as several, whose "Then if" / "if you control" the old
# collectors judged as they triggered (Hadana's Climb, Majestic Myriarch, Alacrian Armory, Does Machines), an effect's
# "if" written as an option (Stormbeacon Blade), Karai's "instead" between two graveyard-targeting returns, and
# Typhoid Mary's "If you discarded a card this turn, you choose one instead", whose mode is chosen as the ability is
# put on the stack. Exact replacements, skipped once the new text is there.
MANUAL = {
    "FiremaneAngel.java": [
        ("""        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(
                        new ConditionalEffect(new SourceIsOnBattlefield(), new GainLifeEffect(1)),
                        "Gain 1 life?"));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT,
                new MayEffect(
                        new ConditionalEffect(new SourceCardInGraveyard(), new GainLifeEffect(1)),
                        "Gain 1 life?"));""",
         """        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsOnBattlefield()),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInGraveyard()),
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));"""),
    ],
    "GatewatchBeacon.java": [
        ("""        addTrigger(Triggers.enters(new PermanentIsPlaneswalkerPredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new ConditionalEffect(
                                new SourceCounterThreshold(1, CounterType.LOYALTY),
                                new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.LOYALTY)),
                        "Move a loyalty counter onto that planeswalker?"));""",
         """        addTrigger(Triggers.enters(new PermanentIsPlaneswalkerPredicate(), ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.LOYALTY)),
                new MayEffect(
                        new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.LOYALTY),
                        "Move a loyalty counter onto that planeswalker?"));"""),
    ],
    "IanTheReckless.java": [
        ("""        addTrigger(Triggers.attacks().withFilter(new PermanentIsModifiedPredicate()),
                new MayEffect(
                        new ConditionalEffect(
                                new SourceMatchesPermanentPredicate(new PermanentIsModifiedPredicate()),
                                SequenceEffect.of(
                                        new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.CONTROLLER),
                                        new DealDamageToAnyTargetEffect(new SourcePower()))),
                        "Have Ian the Reckless deal damage equal to its power to you and any target?"));""",
         """        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceMatchesPermanentPredicate(new PermanentIsModifiedPredicate())),
                new MayEffect(
                        SequenceEffect.of(
                                new DealDamageToPlayersEffect(new SourcePower(), DamageRecipient.CONTROLLER),
                                new DealDamageToAnyTargetEffect(new SourcePower())),
                        "Have Ian the Reckless deal damage equal to its power to you and any target?"));"""),
    ],
    "BloodhallPriest.java": [
        ("""        ConditionalEffect emptyHandDamage = new ConditionalEffect(
                new ControllerHandEmpty(), new DealDamageToAnyTargetEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), emptyHandDamage);
        addTrigger(Triggers.attacks(), emptyHandDamage);""",
         """        TriggerOptions emptyHand = TriggerOptions.DEFAULT.withInterveningIf(new ControllerHandEmpty());
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(2);
        addTrigger(Triggers.enters(ObjectRelation.THIS), emptyHand, damage);
        addTrigger(Triggers.attacks(), emptyHand, damage);"""),
    ],
    "NaomiPillarOfOrder.java": [
        ("""        ConditionalEffect createSamurai = new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanent(new PermanentIsArtifactPredicate()),
                        new ControlsPermanent(new PermanentIsEnchantmentPredicate()))),
                new CreateTokenEffect(""",
         """        TriggerOptions artifactAndEnchantment = TriggerOptions.DEFAULT.withInterveningIf(new AllOf(List.of(
                new ControlsPermanent(new PermanentIsArtifactPredicate()),
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()))));
        CreateTokenEffect createSamurai = (
                new CreateTokenEffect("""),
        ("""        addTrigger(Triggers.enters(ObjectRelation.THIS), createSamurai);
        addTrigger(Triggers.attacks(), createSamurai);""",
         """        addTrigger(Triggers.enters(ObjectRelation.THIS), artifactAndEnchantment, createSamurai);
        addTrigger(Triggers.attacks(), artifactAndEnchantment, createSamurai);"""),
    ],
    "OmnivorousFlytrap.java": [
        ("""        var deliriumAbility = new ConditionalEffect(
                new Delirium(),
                SequenceEffect.of(
                        DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new ConditionalEffect(
                                new CardTypesAmongCardsInGraveyardAtLeast(6),
                                new DoublePlusOneCountersOnTargetCreatureEffect())));

        target(TargetFilters.creature(), 1, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), deliriumAbility)
                .addTrigger(Triggers.attacks(), deliriumAbility);""",
         """        TriggerOptions delirium = TriggerOptions.DEFAULT.withInterveningIf(new Delirium());
        var deliriumAbility = SequenceEffect.of(
                DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                        CounterType.PLUS_ONE_PLUS_ONE, 2),
                new ConditionalEffect(
                        new CardTypesAmongCardsInGraveyardAtLeast(6),
                        new DoublePlusOneCountersOnTargetCreatureEffect()));

        target(TargetFilters.creature(), 1, 2)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), delirium, deliriumAbility)
                .addTrigger(Triggers.attacks(), delirium, deliriumAbility);"""),
    ],
    "HadanasClimb.java": [
        ("""                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ConditionalEffect(""",
         """                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new ConditionalEffect("""),
    ],
    "MajesticMyriarch.java": [
        ("""        // At the beginning of each combat, gain each listed keyword UEOT if you control a
        // creature with that keyword. One ability; each keyword checked at resolution.
        for (Keyword keyword : COMBAT_KEYWORDS) {
            addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                    new ConditionalEffect(
                            new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                    new PermanentIsCreaturePredicate(),
                                    new PermanentHasKeywordPredicate(keyword)))),
                            new GrantKeywordEffect(keyword, GrantScope.SELF)));
        }""",
         """        // At the beginning of each combat, gain each listed keyword UEOT if you control a
        // creature with that keyword. One ability; each keyword checked at resolution.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY), COMBAT_KEYWORDS.stream()
                .map(keyword -> (CardEffect) new ConditionalEffect(
                        new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(keyword)))),
                        new GrantKeywordEffect(keyword, GrantScope.SELF)))
                .toArray(CardEffect[]::new));"""),
    ],
    "CarpetOfFlowers.java": [
        ("""        opponentTarget.addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), ability());
        opponentTarget.addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU), ability());""",
         """        TriggerOptions noManaAddedYet = TriggerOptions.DEFAULT.withInterveningIf(
                new NotCondition(new SourceAddedManaThisTurn()));
        opponentTarget.addTrigger(Triggers.precombatMainPhase(ActivePlayer.YOU), noManaAddedYet, ability());
        opponentTarget.addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU), noManaAddedYet, ability());"""),
        ("""        return new ConditionalEffect(
                new NotCondition(new SourceAddedManaThisTurn()),
                new MayEffect(
                        new AwardAnyColorManaEffect(islands, ManaSpendRestriction.NONE, null,
                                false, true, false, true, false, false),
                        "Add mana with Carpet of Flowers?"));""",
         """        return new MayEffect(
                new AwardAnyColorManaEffect(islands, ManaSpendRestriction.NONE, null,
                        false, true, false, true, false, false),
                "Add mana with Carpet of Flowers?");"""),
    ],
    "AlacrianArmory.java": [
        ("""                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.MOUNT)),
                                new BecomeSaddledUntilEndOfTurnEffect(GrantScope.TARGET)))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ConditionalEffect(""",
         """                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ConditionalEffect(
                                new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.MOUNT)),
                                new BecomeSaddledUntilEndOfTurnEffect(GrantScope.TARGET)),
                        new ConditionalEffect("""),
    ],
    "StormbeaconBlade.java": [
        ("""        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new MinimumMatchingAttackers(3, new PermanentTruePredicate())),
                new DrawCardEffect());""",
         """        // "Draw a card if you control three or more attacking creatures" is the effect's "if", checked as it resolves.
        addTrigger(Triggers.attacks(),
                new ConditionalEffect(new MinimumMatchingAttackers(3, new PermanentTruePredicate()),
                        new DrawCardEffect()));"""),
    ],
    "DoesMachines.java": [
        ("""                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ConditionalEffect(
                                new AllOf(List.of(new SourceClassLevelAtLeast(3),
                                        new NotCondition(new TargetPermanentMatches(new PermanentIsCreaturePredicate())))),""",
         """                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                        new ConditionalEffect(
                                new NotCondition(new TargetPermanentMatches(new PermanentIsCreaturePredicate())),"""),
    ],
    "KaraiFutureOfTheFoot.java": [
        ("""        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ConditionalEffect(
                new NotCondition(sneakCostPaidThisTurn),
                returnCreatureToHand()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ConditionalEffect(
                sneakCostPaidThisTurn,
                returnCreatureToBattlefield()));""",
         """        // "Return target creature card … If her sneak cost was paid this turn, instead return that card to the
        // battlefield": which return happens can't change before the ability resolves, so each is an ability of its
        // own whose event condition picks it, and only one asks for its target (one ability with a shared graveyard
        // target is a later fix).
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(sneakCostPaidThisTurn)),
                returnCreatureToHand());
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(sneakCostPaidThisTurn),
                returnCreatureToBattlefield());"""),
    ],
    "TyphoidMaryFractured.java": [
        ("""        addTrigger(Triggers.attacks(), ConditionalEffect.atTriggerTime(
                new ControllerDiscardedCardThisTurn(), controllerChoice),
                ConditionalEffect.atTriggerTime(
                new NotCondition(new ControllerDiscardedCardThisTurn()), randomChoice));""",
         """        // "Choose one at random. If you discarded a card this turn, you choose one instead": the mode is chosen as the
        // ability is put on the stack (CR 700.2b), so which choice is made is part of the trigger event, an ability of
        // its own for each.
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerDiscardedCardThisTurn()),
                controllerChoice);
        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new ControllerDiscardedCardThisTurn())),
                randomChoice);"""),
    ],
}


def collapse(text):
    return re.sub(r"\s+", " ", text).replace("( ", "(").replace(" )", ")").strip()


def call_end(src, open_paren):
    i, depth = open_paren + 1, 1
    in_str = False
    while depth:
        ch = src[i]
        if in_str:
            if ch == "\\":
                i += 1
            elif ch == '"':
                in_str = False
        elif ch == '"':
            in_str = True
        elif ch == "(":
            depth += 1
        elif ch == ")":
            depth -= 1
        i += 1
    return i


def arg_spans(src, open_paren, close_paren):
    """(start, end) of each top-level argument of the call whose parentheses are at the two indexes."""
    spans, depth, start, in_str = [], 0, open_paren + 1, False
    i = open_paren + 1
    while i < close_paren - 1:
        ch = src[i]
        if in_str:
            if ch == "\\":
                i += 1
            elif ch == '"':
                in_str = False
        elif ch == '"':
            in_str = True
        elif ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
        elif ch == "," and depth == 0:
            spans.append((start, i))
            start = i + 1
        i += 1
    spans.append((start, close_paren - 1))
    return spans


def strip_span(src, span):
    start, end = span
    while start < end and src[start].isspace():
        start += 1
    while end > start and src[end - 1].isspace():
        end -= 1
    return start, end


def fold_unless_and_flags(src):
    src = re.sub(r"\b((?:com\.github\.laxika\.magicalvibes\.model\.effect\.)?)ConditionalEffect\.unless\(",
                 r"new \1ConditionalEffect(", src)
    out, pos = [], 0
    for m in re.finditer(r"new (?:com\.github\.laxika\.magicalvibes\.model\.effect\.)?ConditionalEffect\(", src):
        if m.start() < pos:
            continue
        open_paren = m.end() - 1
        close = call_end(src, open_paren)
        spans = arg_spans(src, open_paren, close)
        if len(spans) == 3:
            third = src[slice(*strip_span(src, spans[2]))]
            if third in ("true", "false"):
                out.append(src[pos:spans[1][1]])
                pos = spans[2][1]
        # nested ConditionalEffects inside are handled on a later pass
    out.append(src[pos:])
    return "".join(out)


def convert_options(name, src):
    wanted = OPTIONS.get(name)
    if not wanted:
        return src, 0
    changed = 0
    while True:
        done = False
        for m in ABILITY.finditer(src):
            open_paren = m.end() - 1
            close = call_end(src, open_paren)
            spans = [strip_span(src, s) for s in arg_spans(src, open_paren, close)]
            if len(spans) < 2:
                continue
            first_effect = 1
            options_span = None
            if src[slice(*spans[1])].startswith("TriggerOptions."):
                options_span = spans[1]
                first_effect = 2
            effects = spans[first_effect:]
            conds = []
            for span in effects:
                text = src[slice(*span)]
                if not text.startswith("new ConditionalEffect("):
                    conds = None
                    break
                inner_open = span[0] + len("new ConditionalEffect")
                inner_close = call_end(src, inner_open)
                if inner_close != span[1]:
                    conds = None
                    break
                inner = [strip_span(src, s) for s in arg_spans(src, inner_open, inner_close)]
                if len(inner) != 2:
                    conds = None
                    break
                conds.append((collapse(src[slice(*inner[0])]), src[slice(*inner[0])], inner[1]))
            if not conds:
                continue
            kinds = [kind for cond, kind in wanted if cond == conds[0][0]]
            if not kinds or any(c[0] != conds[0][0] for c in conds):
                continue
            kind = kinds[0]
            condition_src = conds[0][1]
            options = src[slice(*options_span)] if options_span else "TriggerOptions.DEFAULT"
            existing = EXISTING_TO_EVENT.get(name)
            if existing and ".withInterveningIf(" in options:
                options = options.replace(".withInterveningIf(", ".withEventCondition(", 1)
            elif ".withInterveningIf(" in options and kind == "IIF":
                sys.exit("%s already has an intervening \"if\": %s" % (name, options))
            options += (".withInterveningIf(" if kind == "IIF" else ".withEventCondition(") + condition_src + ")"
            new_effects = [src[slice(*c[2])] for c in conds]
            head = src[m.start():spans[0][1]]
            line_start = src.rfind("\n", 0, m.start()) + 1
            indent = re.match(r"\s*", src[line_start:]).group() + " " * 8
            replacement = head + ",\n" + indent + options + ",\n" + ",\n".join(
                indent + reindent(e, indent) for e in new_effects) + ")"
            src = src[:m.start()] + replacement + src[close:]
            changed += 1
            done = True
            break
        if not done:
            return src, changed


def reindent(text, indent):
    """{@code text} with its continuation lines indented under {@code indent} as they were under its first line."""
    lines = text.split("\n")
    rest = [line for line in lines[1:] if line.strip()]
    if not rest:
        return text
    base = min(len(line) - len(line.lstrip()) for line in rest)
    return "\n".join([lines[0]] + [(indent + " " * 8 + line[base:]) if line.strip() else line for line in lines[1:]])


def ensure_import(src, fqcn):
    simple = fqcn.rsplit(".", 1)[1]
    if re.search(r"^import %s;" % re.escape(fqcn), src, re.M) or not re.search(r"\b%s\b" % simple, src):
        return src
    m = list(re.finditer(r"^import [\w.]+;\n", src, re.M))
    return src[:m[-1].end()] + "import %s;\n" % fqcn + src[m[-1].end():]


def drop_unused_import(src, fqcn):
    simple = fqcn.rsplit(".", 1)[1]
    body = re.sub(r"^import .*\n", "", src, flags=re.M)
    if re.search(r"\b%s\b" % simple, body):
        return src
    return re.sub(r"^import %s;\n" % re.escape(fqcn), "", src, flags=re.M)


def main():
    converted = {}
    unmatched = []
    for top in ("magical-vibes-card", "magical-vibes-card-test"):
        for root, _, files in os.walk(top):
            if "/build/" in root:
                continue
            for name in files:
                if not name.endswith(".java"):
                    continue
                path = os.path.join(root, name)
                src = open(path).read()
                if "ConditionalEffect" not in src and "AttacksAlone" not in src and name not in MANUAL:
                    continue
                new = src
                for _ in range(4):
                    new = fold_unless_and_flags(new)
                if "/src/main/" in path:
                    for before, after in MANUAL.get(name, []):
                        if after not in new:
                            if before not in new:
                                unmatched.append(name)
                                continue
                            new = new.replace(before, after)
                    if name in MANUAL:
                        new = ensure_import(new, "com.github.laxika.magicalvibes.model.trigger.TriggerOptions")
                        new = ensure_import(new, "com.github.laxika.magicalvibes.model.effect.CardEffect")
                        new = ensure_import(new, "com.github.laxika.magicalvibes.model.effect.ConditionalEffect")
                        new = drop_unused_import(new, "com.github.laxika.magicalvibes.model.effect.ConditionalEffect")
                        new = drop_unused_import(new, "com.github.laxika.magicalvibes.model.trigger.TriggerOptions")
                    # "Attacks alone" (exalted, CR 702.83a) is the trigger event, not an intervening "if".
                    new = new.replace(".withInterveningIf(new AttacksAlone())", ".withEventCondition(new AttacksAlone())")
                    new, n = convert_options(name, new)
                    if n:
                        converted[name] = n
                        new = ensure_import(new, "com.github.laxika.magicalvibes.model.trigger.TriggerOptions")
                        new = drop_unused_import(new, "com.github.laxika.magicalvibes.model.effect.ConditionalEffect")
                if new != src:
                    open(path, "w").write(new)
    missing = sorted(set(OPTIONS) - set(converted))
    for name in sorted(converted):
        print("options\t%s\t%d" % (name, converted[name]))
    if missing:
        print("not converted (already done, or the form didn't match): %s" % ", ".join(missing), file=sys.stderr)
    if unmatched:
        sys.exit("hand-converted abilities not in their old form: %s" % ", ".join(sorted(set(unmatched))))


if __name__ == "__main__":
    main()
