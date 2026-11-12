#!/usr/bin/env python3
"""Step 15, run 17z16c3c: the effects the engine's services build with runtime data bound in (read-only).

Lists every construction of an effect record (`new XEffect(...)`, `XEffect.factory(...)`, for every type of
`model/effect/` named `...Effect` or `...FollowUp`) under the engine's `service/` whose arguments name something other
than a literal, a constant or the card's own data (a component of the effect being resolved, a condition, amount,
predicate or filter the method did not build): an id, a card, a player, an evaluated number. A wrapper around an effect
(`MayEffect`, `ConditionalEffect`, `SequenceEffect`, a modal) is not listed; the effect it wraps is. For each site it
prints the file, the method, the effect class, the arguments that are runtime data, where the effect goes (the sinks
its statement names, and those of every later statement of the method that names a variable it was assigned or added
to), and a verdict:

  by where it goes (`AUTOMATIC`, the first sink that applies in this order)
    STACK         it reaches the stack (`stack.add`, `enqueueTrigger`, a reflexive ability): never automatic
    CHOICE        it is carried by a choice context or a pending interaction: never automatic
    DELAYED       the effect of a delayed triggered ability or delayed action the resolving effect creates (CR 603.7c)
    QUESTION      the payload of a question the resolving object asks (`PendingMayAbility`, CR 608.2d); where an
                  accepted "you may" is put on the stack, row 17z16c4b
    CONTINUATION  inserted as a later step of the entry that is resolving (CR 608.2c)
    FLOATING      a continuous effect's or granted ability's record (CR 611.2c)
    INLINE        handed to a handler or service call of the same resolution
    PROGRESS      (any sink) the handler's own effect rebuilt with the progress of its resolution
  by the `RULES` table, for a site no route classified (read in run 17z16c3c)
    ENGINE        a keyword's ability the engine makes
    DELAYED       an engine-made delayed ability or action, a gained ability about the object it was made for
    DEFINITION    the definition of a card, token or room the engine makes
    TEXT          a text-changing effect's rewrite
    STATIC        a static ability's, continuous effect's or restriction's record
    CARD-DATA     nothing determined at runtime is bound (the scan could not see that)
    ACTIVATION    an activated ability's effect fixed as it is activated (noted for step 16)
    INLINE, PROGRESS, QUESTION as above, through a helper the scan does not follow
  UNCLASSIFIED    neither: read it, then convert it or explain it in `RULES`

`CONVERTED` names the constructions run 17z16c3c removed; one that comes back prints `STILL CONSTRUCTED`.

Usage: step15-run17z16c3c-scan.py [--summary] [--unclassified]
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SERVICE = os.path.join(ROOT, "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service")
EFFECTS = os.path.join(ROOT, "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect")

METHOD = re.compile(
    r"^    (?! )(?!(?:if|for|while|switch|else|try|catch|do|return|synchronized|new|throw)\b)"
    r"(?:@\w+ +)*(?:(?:public|private|protected|static|final|synchronized) +)*[\w<>\[\], .?]+ +(\w+) *\(")
IDENT = re.compile(r"(?<![\w.\"])([a-z]\w*)\b(?!\s*->)")
STRING = re.compile(r'"(?:[^"\\]|\\.)*"')

SINKS = [
    ("STACK", re.compile(
        r"\.stack\.add|enqueueTrigger|ReflexiveAbilitySupport\.|QueueReflexiveAbilityEffect|"
        r"queueReflexiveAbilityEffectHandler|[qQ]ueueReflexive|putOnStack|addToStack")),
    ("CHOICE", re.compile(
        r"ChoiceContext\.\w+\(|queueInteraction|beginInteraction|new PendingInteraction\.|new Pending(?!MayAbility)\w+\(|"
        r"[pP]ending(?!MayAbilities)\w*\.(?:add|put)\w*\(|\.pending\w+\s*=")),
    ("QUESTION", re.compile(r"PendingMayAbility|queueMayAbility|pendingMayAbilities")),
    ("DELAYED", re.compile(
        r"queueDelayedAction|new Delayed\w+\(|delayed\w*\.add|addPersistentAbility|registerDelayed")),
    ("CONTINUATION", re.compile(
        r"insertEffectsToResolve|spliceSequenceSteps|replaceEffectToResolve|replaceEffectsToResolve|"
        r"replaceEffectsForBombardment|convertToBombardmentMissileSpell|getEffectsToResolve\(\)\.(?:add|set)")),
    ("FLOATING", re.compile(r"addFloatingEffect|FloatingEffect\(|addGrantedEffect|grant\w*\(|Grant\w*\(")),
    ("INLINE", re.compile(
        r"\w*[hH]andler\w*\.\w+\(|\.resolve\w*\(|[sS]ervice\.\w+\(|[sS]upport\.\w+\(|resolve\w*\(|apply\w*\(")),
    ("RETURN", re.compile(r"^\s*return\b")),
]

ENGINE = "ENGINE"
DELAYED = "DELAYED"
DEFINITION = "DEFINITION"
TEXT = "TEXT"
STATIC = "STATIC"
CARD_DATA = "CARD-DATA"
ACTIVATION = "ACTIVATION"
INLINE = "INLINE"
PROGRESS = "PROGRESS"
QUESTION = "QUESTION"

DELAYED_REASON = ("a delayed triggered ability or delayed action an earlier effect created, put on the stack from what "
                  "game state recorded as it was created (CR 603.7c); the engine makes the entry, no card writes it")

# (file pattern, method pattern or None, effect pattern or None, kind, reason); the first match is the verdict of a
# site no route classified. Read in run 17z16c3c: the handler packages (effect/, input/, interaction/) site by site,
# the collectors and readers (turn/, trigger/, combat/, battlefield/, ability/, spell/, graveyard/) by family.
RULES = [
    (r"effect/TextChangeTransformer\.java", None, None, TEXT,
     "a text-changing effect rewrites an effect's words (CR 612.1); the result is the same ability, reworded"),
    (r"effect/(ConditionEvaluationService|normalfx/LandManaTypeSupport|"
     r"staticfx/GrantChosenBasicLandTypeToOwnLandsEffectHandler)\.java", None, None, CARD_DATA,
     "a static helper of the effect's class is called; no effect is constructed"),
    (r"effect/normalfx/PutKeywordCountersOnControlledCreaturesThenPutPlusOneCountersOnSourceEffectHandler\.java",
     None, None, CARD_DATA, "an effect built to compare with a card's static abilities; never applied"),
    (r"effect/(LayerSystemService|staticfx/.*|normalfx/GrantProtectionSharedByOwnCreaturesUntilEndOfTurnEffectHandler|"
     r"normalfx/GrantPlayerStaticEffectsUntilEndOfTurnEffectHandler|"
     r"normalfx/GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffectHandler|"
     r"normalfx/CreaturesOfChosenTypeMustAttackThisTurnEffectHandler|"
     r"normalfx/TargetPlayersCreaturesMustAttackThisTurnEffectHandler|"
     r"normalfx/PerpetuallyBoostTopCreatureCardInLibraryEffectHandler)\.java|input/ChoiceHandlerService\.java|"
     r"turn/UntapStepService\.java|spell/SpellCastingService\.java|battlefield/AsEntersInteractionService\.java",
     None, None, STATIC,
     "a static ability's, a continuous effect's or a restriction's record, kept on a permanent, a card or the game "
     "for as long as it applies (CR 611.2c for one made by a resolving object); not an ability on the stack"),
    (r"effect/normalfx/(Conjure\w+SpellbookEffectHandler|VentureIntoDungeonEffectHandler|"
     r"TokenCreationReplacementSupport|ApplejackFamilyGatheringEffectHandler|DraftFromSpellbookEffectHandler|"
     r"ExileTargetCardFromGraveyardAndCreateTokenCopyEffectHandler)\.java|"
     r"effect/HandCastCardCharacteristicsService\.java|input/CardChoiceHandlerService\.java|"
     r"combat/attack/CombatAttackService\.java", r"creature|buryInBooks|roomEffects|creatureToken|copyKind|"
     r"soldierToken|jinnieFayToken|toyToken|resolve|zombieWithStatsOf|effectiveCard|handlePerpetualOffspringCardChosen|"
     r"conjureDoubleTeamDuplicate", None, DEFINITION,
     "the definition of a card, token or dungeon room the engine makes (a spellbook card, a room's ability, a token "
     "profile, offspring's kicker), or the token a replacement effect makes instead; card data of the made object"),
    (r"effect/normalfx/(CreateTokenEffectHandler|ExileDyingCreatureCardAndCreateTokenEffectHandler|"
     r"ExileTopCardAndCreateTokenEffectHandler|GraveyardReturnSupport|GainControlOfTargetUntilRansomEffectHandler)\.java",
     r"deathReturn|bindReturn|resolve|grantOnDeathEffect", None, DELAYED,
     "the ability a resolving effect gives a permanent it made or changed (\"when this token dies, return the exiled "
     "card\", the ransom ability), about the object determined as it was made (CR 603.7c)"),
    (r"effect/normalfx/(DemonstrateEffectHandler|DestroyTargetCreatureThenAmassGoblinsEffectHandler|"
     r"TransmuteArtifactSelectedCardEffectHandler|TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffectHandler|"
     r"FlipCoinsPerHeadsEffectHandler|CumulativeUpkeepEffectHandler|PayXManaSearchLibraryEffectHandler|"
     r"BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffectHandler)\.java|input/MayCastHandlerService\.java|"
     r"input/MayPenaltyChoiceHandlerService\.java|interaction/EachPlayerMayPutCardFromHandChoiceInteractionHandler\.java",
     None, None, INLINE,
     "resolved in the resolution that determined the data: handed to a handler, or inserted as a later step of the "
     "entry that is resolving, through a helper the scan does not follow (CR 608.2c); never another stack object"),
    (r"effect/normalfx/DestructionSupport\.java", r"completeForcedCostOrElse", None, PROGRESS,
     "the forced cost still to pay, asked again in the same resolution"),
    (r"effect/normalfx/ExileReducedCastSupport\.java|effect/mayfx/ReturnTargetPermanentToHandThenMayCopyEffectHandler\.java",
     None, None, QUESTION,
     "the payload of a question the resolving object asks (CR 608.2d), applied in the resolution that asked"),
    (r"effect/normalfx/(EachPlayerExilesCardFromGraveyardThenEffectHandler|"
     r"ExilePermanentThenExileMatchingPermanentsEffectHandler)\.java|input/PermanentChoiceBattlefieldHandlerService\.java",
     None, None, CARD_DATA,
     "the reflexive ability's effect is the card's own (its `thenEffect`, its matching filter carried by the choice, "
     "an amount that reads the record: `SacrificedPermanentPower`); nothing determined at runtime is bound in"),
    (r"effect/normalfx/RemoveTimeCounterFromExiledCardEffectHandler\.java|graveyard/GraveyardService\.java|"
     r"ability/AbilityActivationService\.java", None, None, ENGINE,
     "a keyword's ability the engine makes (suspend's \"when the last time counter is removed, cast it\", CR 702.62a; "
     "madness, CR 702.35a; ninjutsu, CR 702.49a): the rules, not a card, write its effect"),
    (r"ability/ActivatedAbilityExecutionService\.java", r"snapshotEffects", None, ACTIVATION,
     "an activated ability's effect given the permanent that granted it or its source's choice as it is activated; "
     "no trigger event is involved (step 15's subject). Keyed by the effect's class: noted for step 16"),
    (r"turn/StepTriggerService\.java|turn/TurnCleanupService\.java|trigger/TriggerCollectionService\.java|"
     r"combat/.*|battlefield/(CreatureControlService|PermanentRemovalService)\.java|DamagePreventionService\.java",
     None, None, DELAYED, DELAYED_REASON),
]

# Constructions the run removed: (file, effect) -> what replaced it. Reported as STILL CONSTRUCTED if one comes back.
CONVERTED = {
    ("effect/normalfx/EarthbendTargetLandThenFightEffectHandler.java", "FightTargetsEffect"):
        "`FightTargetsEffect.ofReflexiveAbilityTargets()` reads the reflexive entry's own targets (Earth Rumble)",
    ("effect/normalfx/NihiloorTapAndStealEffectHandler.java", "GainControlOfTargetEffect"):
        "a constant effect; that player and the tapped creature are the record's triggering player and permanent",
    ("effect/normalfx/RevealUntilNonlandBottomThenDealManaValueDamageEffectHandler.java",
     "RevealUntilNonlandBottomThenDealManaValueDamageEffect"):
        "the card's own step repeated; the targets left are the entry's picks (Kaboom!)",
    ("effect/normalfx/EachPlayerMayPutCountersOnCreatureEffectHandler.java",
     "PutCountersOnTargetPermanentThenReflexiveEffect"):
        "the handler and its effect are deleted (no card wrote it; Orzhov Advokist and Agitator Ant write "
        "`EachPlayerMayPutCountersOnCreatureAndRestrictAttacksEffect`)",
    ("input/GraveyardChoiceHandlerService.java", "BecomeCopyOfCardUntilEndOfTurnEffect"):
        "`BecomeCopyOfCardUntilEndOfTurnEffect.ofQuestionCard()`; the question names the exiled card (Lazav)",
    ("effect/normalfx/CreateTokenThenEffectHandler.java", "QueueReflexiveAbilityEffect"):
        "`QueueReflexiveAbilityEffect.aboutPickedPermanent`; the tokens are the entry's picks",
    ("effect/normalfx/CreateTokenAttachedToTargetThenEffectHandler.java", "QueueReflexiveAbilityEffect"):
        "`QueueReflexiveAbilityEffect.aboutEntryTarget` (Curse of the Werefox)",
    ("effect/normalfx/RandomPlayerThenFightsTargetCreatureEffectHandler.java", "QueueReflexiveAbilityEffect"):
        "a constant effect; the player chosen at random is the record's triggering player (Strax)",
    ("effect/normalfx/ExileTopCardMayPlayThisTurnThenDealManaValueDamageEffectHandler.java",
     "QueueReflexiveAbilityEffect"):
        "found by the scan: the damage reads `EventValue` (Hidetsugu, Devouring Chaos)",
    ("interaction/SearchLibraryAndOrGraveyardChoiceInteractionHandler.java",
     "AttachSelectedEquipmentToCreatureEffect"):
        "found by the scan: the Equipment is the reflexive entry's pick",
    ("effect/normalfx/AttachSelectedAurasToLegalTargetsEffectHandler.java",
     "AttachSelectedAurasToLegalTargetsEffect"):
        "the Auras left are the entry's picks",
    ("effect/normalfx/ReturnSourceAuraToCreatureOrAsNonAuraEffectHandler.java", "DealDamageToPlayerEffect"):
        "found by the scan: the handler, its effect and `DealDamageToPlayerEffect` are deleted (nothing made them)",
}



def effect_types():
    names = set()
    for name in os.listdir(EFFECTS):
        if name.endswith(".java"):
            names.add(name[:-5])
    return names


def without_comments(text):
    """The source with comments blanked, offsets and line numbers kept."""
    def blank(match):
        if match.group(0).startswith('"'):
            return match.group(0)
        return re.sub(r"[^\n]", " ", match.group(0))
    return re.sub(r'"(?:[^"\\\n]|\\.)*"|//[^\n]*|/\*.*?\*/', blank, text, flags=re.S)


def matching_paren(text, start):
    depth = 0
    i = start
    while i < len(text):
        c = text[i]
        if c == '"':
            m = STRING.match(text, i)
            if m:
                i = m.end()
                continue
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


WRAPPERS = {"ConditionalEffect", "MayEffect", "SequenceEffect", "TriggeredModalEffect", "ChooseOneEffect",
            "StateTriggerEffect", "ChooseOneAtTriggerTimeEffect", "MayPayManaEffect"}
AUTHORED_TYPES = {"Condition", "DynamicAmount", "CardPredicate", "PermanentPredicate", "PlayerPredicate",
                  "TargetFilter", "StackEntryPredicate", "TargetPredicate", "TriggeredAbility",
                  "EffectRegistration", "TargetSpec"}


def card_data_variables(text, types):
    """Locals and parameters typed as an effect, or as a condition, amount, predicate or filter that is not built
    in the method (`= new ...`): a value read from one is the card's own data."""
    names = {"effect"}
    for m in re.finditer(r"\b(\w+)(?:<[\w<>, ?]+>)? +(\w+)\s*(=(?!=)[^;]*|\)|,|;|:)", text):
        if m.group(1) in types or m.group(1) == "CardEffect":
            if m.group(1) in types and not (m.group(1).endswith("Effect") or m.group(1).endswith("FollowUp")):
                continue
            names.add(m.group(2))
        elif m.group(1) in AUTHORED_TYPES and "new " not in m.group(3) and "(" not in m.group(3).replace("()", ""):
            names.add(m.group(2))
    for m in re.finditer(r"\b(?:var|final var) +(\w+)\s*=\s*\((\w+)\)", text):
        if m.group(2) in types:
            names.add(m.group(1))
    for m in re.finditer(r"instanceof (\w+) (\w+)", text):
        if m.group(1) in types:
            names.add(m.group(2))
    return names


def runtime_arguments(args, card_vars):
    stripped = STRING.sub('""', args)
    stripped = re.sub(r"\b(\w+)::\w+", "", stripped)
    runtime = []
    for m in IDENT.finditer(stripped):
        name = m.group(1)
        if name in ("new", "true", "false", "null", "this", "instanceof", "int", "boolean", "long", "com", "java"):
            continue
        if name in card_vars:
            continue
        before = stripped[:m.start()].rstrip()
        if before.endswith("."):
            continue
        rest = stripped[m.end():].lstrip()
        if rest.startswith("("):
            # A bare method call: a helper of the handler computing a runtime value.
            runtime.append(name + "()")
            continue
        runtime.append(name)
    return sorted(set(runtime))


def statement_bounds(text, position, lower, upper):
    start = position
    depth = 0
    while start > lower:
        c = text[start - 1]
        if c == ")":
            depth += 1
        elif c == "(":
            if depth == 0:
                pass
            else:
                depth -= 1
        elif c in ";{}" and depth == 0:
            break
        start -= 1
    end = position
    depth = 0
    while end < upper:
        c = text[end]
        if c == '"':
            m = STRING.match(text, end)
            if m:
                end = m.end()
                continue
        if c in "({":
            depth += 1
        elif c in ")}":
            depth -= 1
            if depth < 0:
                break
        elif c == ";" and depth == 0:
            break
        end += 1
    return start, end


DECLARED = re.compile(r"^\s*(?:final\s+)?(?:[\w<>\[\], .?]+\s+)?(\w+)\s*=(?!=)")
ADDED = re.compile(r"\b(\w+)\.(?:add|addAll|put|addFirst|addLast)\(")


def sinks_of(text, position, lower, upper):
    """Where the effect constructed at `position` goes: the sinks its statement names, and those of every later
    statement of the method that names a variable it was assigned or added to."""
    start, end = statement_bounds(text, position, lower, upper)
    statement = text[start:end]
    found = set()
    tracked = set()
    pending = [statement]
    visited = set()
    while pending:
        current = pending.pop()
        if current in visited:
            continue
        visited.add(current)
        for label, pattern in SINKS:
            if pattern.search(current):
                found.add(label)
        names = set()
        declared = DECLARED.match(current)
        if declared and declared.group(1) not in ("return",):
            names.add(declared.group(1))
        for added in ADDED.finditer(current):
            if added.group(1)[0].islower() and added.group(1) not in ("gameData", "entry", "stack"):
                names.add(added.group(1))
        for name in names - tracked:
            tracked.add(name)
            for use in re.finditer(r"\b" + re.escape(name) + r"\b", text[end:upper]):
                use_start, use_end = statement_bounds(text, end + use.start(), lower, upper)
                pending.append(text[use_start:use_end])
    return found


def scan():
    types = effect_types()
    pattern = re.compile(r"(?:new (\w+)|\b([A-Z]\w+)\.([a-z]\w*))\(")
    sites = []
    for directory, _, files in os.walk(SERVICE):
        for file_name in sorted(files):
            if not file_name.endswith(".java"):
                continue
            path = os.path.join(directory, file_name)
            relative = os.path.relpath(path, SERVICE)
            text = without_comments(open(path, encoding="utf-8").read())
            card_vars = card_data_variables(text, types)
            line_starts = [0]
            for line in text.split("\n"):
                line_starts.append(line_starts[-1] + len(line) + 1)
            methods = []
            method_starts = []
            offset = 0
            for line in text.split("\n"):
                m = METHOD.match(line)
                if m:
                    methods.append((offset, m.group(1)))
                    method_starts.append(offset)
                offset += len(line) + 1
            covered_until = -1
            for m in pattern.finditer(text):
                name = m.group(1) or m.group(2)
                if name not in types or not (name.endswith("Effect") or name.endswith("FollowUp")):
                    continue
                if m.start() < covered_until:
                    continue
                open_paren = m.end() - 1
                close = matching_paren(text, open_paren)
                if close < 0:
                    continue
                if name in WRAPPERS:
                    # A wrapper is card data around an effect; the effect it wraps is scanned on its own.
                    continue
                runtime = runtime_arguments(text[open_paren + 1:close], card_vars)
                if not runtime:
                    continue
                covered_until = close
                method = "?"
                for start, method_name in methods:
                    if start <= m.start():
                        method = method_name
                lower = max([start for start in method_starts if start <= m.start()], default=0)
                upper = min([start for start in method_starts if start > m.start()], default=len(text))
                sinks = sinks_of(text, m.start(), lower, upper)
                line_no = text.count("\n", 0, m.start()) + 1
                sites.append((relative, method, name, line_no, runtime, sinks))
    return sites


def rule_for(relative, method, name):
    for file_pattern, method_pattern, effect_pattern, kind, reason in RULES:
        if (re.fullmatch(file_pattern, relative)
                and (method_pattern is None or re.fullmatch(method_pattern, method))
                and (effect_pattern is None or re.fullmatch(effect_pattern, name))):
            return (kind, reason)
    return None


def route_of(sinks):
    for label in ("STACK", "CHOICE", "DELAYED", "QUESTION", "RETURN", "CONTINUATION", "FLOATING", "INLINE"):
        if label in sinks:
            return label
    return "OTHER"


AUTOMATIC = {
    "DELAYED": "the effect of a delayed triggered ability the resolving effect creates, about the object it "
               "determined as it was created (CR 603.7c); the engine makes the ability, no card writes its effect",
    "QUESTION": "the payload of a question the resolving object asks (CR 608.2d), applied by its may-handler in "
                "the resolution that asked; where an accepted 'you may' is put on the stack, row 17z16c4b",
    "CONTINUATION": "inserted as a later step of the entry that is resolving, with data determined in this "
                    "resolution (CR 608.2c); never another object on the stack",
    "PROGRESS": "the handler's own effect, rebuilt with the progress of the resolution it is a step of (the players "
                "still to be asked, what was chosen so far); the same stack object, CR 608.2c",
    "INLINE": "passed to a handler or service call of the same resolution; never an object on the stack",
    "FLOATING": "a continuous effect's or a granted ability's record, fixed as the effect is created (CR 611.2c)",
}


def main():
    sites = scan()
    summary = "--summary" in sys.argv
    only_unclassified = "--unclassified" in sys.argv
    counts = {}
    seen = set()
    for relative, method, name, line_no, runtime, sinks in sites:
        verdict = None
        seen.add((relative, name))
        route = route_of(sinks)
        if os.path.basename(relative) in (name + "Handler.java", name + "MayHandler.java",
                                          name.removesuffix("Effect") + "Handler.java"):
            route = "PROGRESS"
        if verdict is None and route in AUTOMATIC:
            verdict = (route, AUTOMATIC[route])
        if verdict is None:
            verdict = rule_for(relative, method, name)
        kind = verdict[0] if verdict else "UNCLASSIFIED " + route
        counts[kind] = counts.get(kind, 0) + 1
        if summary or (only_unclassified and verdict):
            continue
        print(f"{relative}:{line_no} {method}: {name}({', '.join(runtime)}) -> {'+'.join(sorted(sinks)) or '?'}")
        print(f"    {kind}" + (f": {verdict[1]}" if verdict else ""))
    for key in sorted(CONVERTED):
        state = "STILL CONSTRUCTED" if key in seen else "gone"
        if not summary or key in seen:
            print(f"CONVERTED {key[0]} {key[1]}: {state} ({CONVERTED[key]})")
    print(f"sites: {len(sites)}")
    for kind in sorted(counts):
        print(f"  {kind}: {counts[kind]}")
    print(f"converted (gone): {sum(1 for key in CONVERTED if key not in seen)} of {len(CONVERTED)}")


if __name__ == "__main__":
    main()
