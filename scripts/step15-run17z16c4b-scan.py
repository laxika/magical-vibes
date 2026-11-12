#!/usr/bin/env python3
"""Step 15, run 17z16c4b: every question asked with a `PendingMayAbility`, who asks it and what its answer does
(read-only).

A triggered ability that says "you may" goes on the stack when it triggers and the choice is made as it resolves
(CR 603.5). The scan lists

  producers   every `new PendingMayAbility(...)` and `queueMayAbilityForPlayer(...)` of the engine and domain main
              sources: the file, the method, the payload effect types it could resolve from the source text, the
              answer route and a verdict;
  consumers   every place the code that answers such a question puts a `TRIGGERED_ABILITY` entry on the stack or
              routes one toward it (`service/effect/mayfx`, `service/input/May*.java`).

Answer routes (how `MayAbilityHandlerService.handleMayAbilityChosen` applies the answer)

  RESOLUTION  the producer sets `resolvingMayEffectFromStack`: the answer re-runs the step of the entry that is
              resolving
  HANDLER     a payload type has a `MayEffectHandlerBean` (`service/effect/mayfx`), which applies the answer
  SPECIAL     a pending pile separation, Raging River, bend or break, Word of Command, a pending Equipment
              attachment or the cast-from-library offer takes the answer (`SPECIAL_ROUTES`)
  GENERIC     none of those: the accepted payload is carried out in the resolution that asked
              (`MayAbilityHandlerService.resolveAcceptedInAskingResolution`), never put on the stack

Verdicts

  (a) TRIGGER      a triggered ability's "you may" asked before the ability is on the stack: the row's target, and
                   an error of this scan once the row is done
  (b) REPLACEMENT  a replacement or prevention effect's option (CR 614.1, 615.1), asked as the event happens
      AS-ENTERS    an "as this enters" choice (CR 614.1c), asked as the permanent enters
      COST         a cost or a casting or declaration choice (CR 601.2b, 508.1, 509.1)
      PREGAME      an action before the game begins or as a hand is kept (CR 103.6)
      TURN-BASED   a choice of a turn-based action (the untap step's "you may choose not to untap", CR 502.3)
      AS-ATTACKS   "you may exert this creature as it attacks" (CR 701.43a), asked as attackers are declared
      DRAW         an option of a static ability that applies as a card is drawn (miracle's reveal, CR 702.94a;
                   "you may reveal ... as you draw it")
      RESOLVING    an optional action of a spell or ability that is already resolving (CR 608.2d), asked by its
                   effect handler or by the handler of an answer it waited for
  (c) ON-STACK     the question of an entry on the stack, asked as that entry resolves (CR 603.5): route RESOLUTION

A producer's verdict comes from where it is: `service/effect/normalfx`, `service/effect/mayfx`, `service/input` and
`service/interaction` are the handlers of a resolving object and of its answers (RESOLVING, or ON-STACK with the
flag); every other producer needs an entry in `RULES`, written after reading the method and, where a card decides
it, the card's Oracle text. A producer with no verdict prints `UNCLASSIFIED`.

`CONSUMERS` explains each stacked entry of the answering code; one it does not name prints `UNEXPLAINED`.

Usage: step15-run17z16c4b-scan.py [--summary] [--generic] [--unclassified]
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCES = [
    os.path.join(ROOT, "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes"),
    os.path.join(ROOT, "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes"),
]
MAYFX = os.path.join(SOURCES[0], "service/effect/mayfx")

METHOD = re.compile(
    r"^    (?! )(?!(?:if|for|while|switch|else|try|catch|do|return|synchronized|new|throw)\b)"
    r"(?:@\w+ +)*(?:(?:public|private|protected|static|final|synchronized) +)*[\w<>\[\], .?]+ +(\w+) *\(")
PRODUCER = re.compile(r"new (?:[\w.]+\.)?PendingMayAbility\(|queueMayAbilityForPlayer\(")
STACKED = re.compile(r"gameData\.stack\.add\(|enqueueTrigger\(|ReflexiveAbilitySupport\.route\(")
RESOLVING_DIRS = ("service/effect/normalfx/", "service/effect/mayfx/", "service/input/", "service/interaction/")
NOT_EFFECTS = {"ArrayList", "List", "LinkedList", "MayEffect", "UUID", "String", "Card", "Permanent", "StackEntry",
               "PendingMayAbility", "Integer", "CardEffect", "var"}

REPLACEMENT = "REPLACEMENT"
AS_ENTERS = "AS-ENTERS"
COST = "COST"
PREGAME = "PREGAME"
TURN_BASED = "TURN-BASED"
AS_ATTACKS = "AS-ATTACKS"
DRAW = "DRAW"
RESOLVING = "RESOLVING"
ON_STACK = "ON-STACK"
TRIGGER = "TRIGGER"

# (file suffix, method) -> (verdict, reason): the producers outside the handlers of a resolving object, each read.
RULES = {
    ("model/GameData.java", "queueMayAbilityForPlayer"): (
        None, "the helper itself; its callers are listed"),
    ("service/DamagePreventionService.java", "applyChannelHarmPrevention"): (
        REPLACEMENT, "Channel Harm's prevention effect: \"If damage is prevented this way, you may have Channel "
                     "Harm deal that much damage to target creature\", asked as the damage is prevented"),
    ("service/DamagePreventionService.java", "queueClericPreventionChoice"): (
        REPLACEMENT, "\"If a source would deal damage to you, you may prevent X of that damage\" (a prevention "
                     "effect, CR 615.1), asked as the damage would be dealt"),
    ("service/combat/CombatDamageService.java", "queueCombatCreatureRedirectChoice"): (
        REPLACEMENT, "\"you may have all damage that would be dealt to creatures be dealt to you instead\", "
                     "asked as combat damage would be dealt"),
    ("service/DrawService.java", "resolveCurrentDrawCard"): (
        REPLACEMENT, "a draw replacement the drawing player may apply (\"you may ... instead\"), as the card "
                     "would be drawn"),
    ("service/DrawService.java", "resolveUnpredictableCycloneDrawReplacement"): (
        REPLACEMENT, "Unpredictable Cyclone replaces the cycling draw; \"you may cast it\" is part of that "
                     "replacement"),
    ("service/DrawService.java", "beginZursWeirdingOffer"): (
        REPLACEMENT, "Zur's Weirding: \"any other player may pay 2 life. If a player does, put that card into "
                     "its owner's graveyard instead\""),
    ("service/DrawService.java", "resolveChosenDrawReplacementSource"): (
        REPLACEMENT, "the draw replacement its player chose to apply first (CR 616.1)"),
    ("service/DrawService.java", "queueMaySkipDrawReplacement"): (
        REPLACEMENT, "\"you may skip that draw\" replaces the draw"),
    ("service/DrawService.java", "offerMiracleReveal"): (
        DRAW, "miracle: \"you may reveal this card from your hand as you draw it\"; the cast is the triggered "
              "ability \"when you do\" (CR 702.94a), which `MiracleRevealHandler` puts on the stack"),
    ("service/DrawService.java", "revealDrawnCardByStaticAbilities"): (
        DRAW, "a static \"you may reveal ... as you draw it\" (CR 121.9), asked during the draw"),
    ("service/MulliganService.java", "queueMulliganActionChoice"): (
        PREGAME, "an action a card offers as its player would mulligan or keeps a hand"),
    ("service/MulliganService.java", "startGame"): (
        PREGAME, "an opening-hand action (\"you may begin the game with it on the battlefield\", a secret mission, "
                 "a legacy word): CR 103.6, not a triggered ability"),
    ("service/battle/BattleDefeatSupport.java", "resolveDefeat"): (
        RESOLVING, "the battle's defeat ability is on the stack and resolving (`resolveDefeat(gameData, entry)`): "
                   "\"exile it, then you may cast it transformed\" (CR 310.12b)"),
    ("service/battlefield/AsEntersDiscardSupport.java", "begin"): (
        AS_ENTERS, "\"as this enters, you may discard any number of creature cards\""),
    ("service/battlefield/AsEntersInteractionService.java", "handleCreatureEnteredBattlefield"): (
        AS_ENTERS, "tribute (\"as this creature enters, an opponent of your choice may put N +1/+1 counters on "
                   "it\", CR 702.104a) and \"as this enters, you may return a permanent you control to its owner's "
                   "hand\""),
    ("service/battlefield/BattlefieldPlacementService.java", "beginUnleashChoice"): (
        AS_ENTERS, "unleash: \"you may have this permanent enter with a +1/+1 counter on it\" (CR 702.98a)"),
    ("service/battlefield/BattlefieldPlacementService.java", "beginRiotChoices"): (
        AS_ENTERS, "riot: \"you may have this permanent enter with an additional +1/+1 counter on it. If you "
                   "don't, it gains haste\" (CR 702.136a)"),
    ("service/battlefield/BattlefieldPlacementService.java", "applyRevealSubtypeOrEntersTapped"): (
        AS_ENTERS, "\"as this land enters, you may reveal a [subtype] card from your hand. If you don't, it enters "
                   "tapped\""),
    ("service/battlefield/BattlefieldPlacementService.java", "applyRevealSubtypeOrEntersWithCounters"): (
        AS_ENTERS, "\"as this enters, you may reveal a [subtype] card from your hand. If you do, it enters with a "
                   "counter\""),
    ("service/battlefield/BattlefieldPlacementService.java", "applyMayPayLifeOrEntersTapped"): (
        AS_ENTERS, "\"as this land enters, you may pay N life. If you don't, it enters tapped\""),
    ("service/battlefield/CloneService.java", "prepareCloneReplacementEffect"): (
        AS_ENTERS, "\"you may have this enter as a copy of ...\""),
    ("service/battlefield/CloneService.java", "prepareMimeoplasmReplacementEffect"): (
        AS_ENTERS, "\"as this enters, you may exile two creature cards from graveyards. If you do, it enters as a "
                   "copy of one of them\""),
    ("service/battlefield/CloneService.java", "prepareGraveyardCloneReplacementEffect"): (
        AS_ENTERS, "\"you may have this enter as a copy of any creature card in a graveyard\""),
    ("service/battlefield/CloneService.java", "prepareFixedGraveyardCloneReplacementEffect"): (
        AS_ENTERS, "\"you may have this enter as a copy of any creature card in a graveyard\""),
    ("service/battlefield/PermanentRemovalService.java", "offerMayLibraryReplacement"): (
        REPLACEMENT, "\"if this would die, you may put it on top of its owner's library instead\""),
    ("service/combat/attack/CombatAttackService.java", "putSelfAttackAbilityOnStack"): (
        AS_ATTACKS, "exert: a lone \"you may\" whose effect says it exerts as it attacks "
                    "(`CardEffect.exertsAsItAttacks()`, the card's `SkipNextUntapEffect.exert()`) is chosen as the "
                    "creature is declared as an attacker, so it is no ability on the stack; every other "
                    "\"whenever this attacks, you may\" of this method goes on the stack with its `MayEffect`"),
    ("service/combat/block/CombatBlockService.java", "processBlockerDeclaration"): (
        COST, "the costs of the proposed blocks (CR 509.1d), paid or the declaration is taken back"),
    ("service/effect/ExiledCreatureCopyOnEnterService.java", "prepare"): (
        AS_ENTERS, "\"you may have this enter as a copy of a creature card in exile with a takeover counter\""),
    ("service/effect/entryfx/MayHaveThisLandEnterTappedForRadCountersEntryHandler.java", "apply"): (
        AS_ENTERS, "\"as this land enters, you may have it enter tapped. If you do, you get rad counters\": an "
                   "entry replacement handler"),
    ("service/epic/EpicService.java", "createUpkeepCopy"): (
        RESOLVING, "epic's copy is made by the delayed upkeep ability (CR 702.50a); \"you may choose new targets "
                   "for the copy\" is asked as the copy is created (CR 707.10c)"),
    ("service/paradigm/ParadigmService.java", "createCopyAndQueueMayCast"): (
        RESOLVING, "paradigm's first-main-phase ability is resolving (`createCopyAndQueueMayCast(gameData, "
                   "entry)`): \"you may cast a copy of this card from exile\""),
    ("service/spell/SpellCastingService.java", "queueOpponentModeChoice"): (
        COST, "\"an opponent chooses one\" (Library of Lat-Nam, Fatal Lore, Misfortune): the mode is chosen as "
              "the spell is cast (CR 601.2b)"),
    ("service/turn/StepTriggerService.java", "handleDrawStep"): (
        REPLACEMENT, "a replacement of the draw step's draw (\"if you would draw a card during your draw step, "
                     "you may instead ...\")"),
    ("service/turn/TurnProgressionService.java", "queueTimeVaultChoice"): (
        REPLACEMENT, "Time Vault: \"if you would begin your turn while this is tapped, you may skip that turn "
                     "instead\""),
    ("service/turn/UntapStepService.java", "untapPermanents"): (
        TURN_BASED, "\"you may choose not to untap this during your untap step\""),
}

# (file suffix, method) -> [(text the payload expression contains, the effect types it is, read by hand)]: the
# payloads the scan cannot type from the source text (a helper's result, a field, a list built earlier).
PAYLOADS = {
    ("service/MulliganService.java", "startGame"): [
        ("may.wrapped()", ["PregameChoiceEffect"])],
    ("service/battlefield/PermanentRemovalService.java", "offerMayLibraryReplacement"): [
        ("replacement", ["PutOnTopOfLibraryInsteadOfDyingEffect"])],
    ("service/combat/attack/CombatAttackService.java", "putSelfAttackAbilityOnStack"): [
        ("effects.getFirst()", ["SkipNextUntapEffect.exert()", "QueueReflexiveAbilityEffect"])],
    ("service/effect/mayfx/BreathstealersCryptDrawReplacementHandler.java", "afterDraw"): [
        ("crypt.effect()", ["BreathstealersCryptDrawReplacementEffect"])],
    ("service/effect/mayfx/DealDamageToTargetCreatureOrPlaneswalkerUnlessControllerTakesDamageHandler.java",
     "handle"): [
        ("controllerDamageFollowUp()", ["DiscardAndDrawCardEffect"])],
    ("service/effect/normalfx/CopyCardsExiledWithSourceAndMayCastCopiesEffectHandler.java", "resolve"): [
        ("copyCastChoice(", ["MayCastCopyWithNormalCostEffect", "MayCastCopyWithManaCostEffect",
                             "MayCastCopyWithoutPayingManaCostEffect"])],
    ("service/effect/normalfx/CopyControllerActivatedAbilityEffectHandler.java", "resolve"): [
        ("retargetEffect", ["CopyAbilityRetargetEffect", "CopyActivatedAbilityRetargetEffect"])],
    ("service/effect/normalfx/MayCastCardExiledWithSourceEffectHandler.java", "resolve"): [
        ("offer", ["MayPlayExiledCardWithoutPayingManaCostEffect", "MayCastExiledCardWithNormalCostEffect"])],
    ("service/effect/normalfx/MayCastInstantOrSorceryCardsExiledWithSourceEffectHandler.java", "resolve"): [
        ("offer", ["MayPlayExiledCardWithoutPayingManaCostEffect", "MayCastExiledCardWithNormalCostEffect"])],
    ("service/effect/normalfx/MayCastLesserSpellWithSharedTypeOrCreateTokenEffectHandler.java", "resolve"): [
        ("effects", ["MayCastLesserSpellWithSharedTypeOrCreateTokenEffect",
                     "MayCastFromHandWithoutPayingManaCostEffect"])],
    ("service/effect/normalfx/RandomPlayerExilesInstantOrSorceryAndMayCastCopyEffectHandler.java",
     "resolveChosenCard"): [
        ("COPY_EFFECT", ["ExileTargetCardFromGraveyardAndMayCastCopyEffect"])],
    ("service/input/ChoiceHandlerService.java", "handleLiarsPendulumChoice"): [
        ("revealEffects", ["RevealTargetHandEffect", "DrawCardEffect"])],
    ("service/interaction/InvokeCalamityCastChoiceInteractionHandler.java", "handleAnswer"): [
        ("effects", ["CastInvokeCalamityChosenSpellEffect", "MayCastFromHandWithoutPayingManaCostEffect"])],
}

# Answer routes that are not a may handler: the payloads `handleMayAbilityChosen` hands to a pending structure.
SPECIAL_ROUTES = {
    ("service/effect/normalfx/DestructionSupport.java", "completePileSeparationStep1"): "a pending pile separation",
    ("service/effect/normalfx/FightOrFlightSupport.java", "completePileSeparationStep1"):
        "a pending pile separation",
    ("service/effect/normalfx/StandOrFallSupport.java", "completePileSeparationStep1"): "a pending pile separation",
    ("service/effect/normalfx/GraveyardReturnSupport.java", "completeCardPileSeparationStep1"):
        "a pending pile separation",
    ("service/effect/normalfx/AbstractPerformanceEffectHandler.java", "resolve"): "a pending pile separation",
    ("service/effect/normalfx/BrilliantUltimatumSupport.java", "beginPileChoice"): "a pending pile separation",
    ("service/effect/normalfx/MakeAnExampleEffectHandler.java", "completePileSeparationStep1"):
        "a pending pile separation",
    ("service/effect/normalfx/BendOrBreakEffectHandler.java", "beginPileChoice"): "a pending bend or break",
    ("service/effect/normalfx/RagingRiverEffectHandler.java", "beginAttackerChoice"): "a pending Raging River",
    ("service/effect/normalfx/GraveyardReturnSupport.java", "resolvePreTargetedById"):
        "a pending Equipment attachment",
    ("service/effect/normalfx/CreateTokenAndAttachTriggeringAuraEffectHandler.java", "resolve"):
        "a pending Equipment attachment",
    ("service/input/GraveyardChoiceHandlerService.java", "handleGraveyardCardChosen"):
        "a pending Equipment attachment",
    ("service/input/CardChoiceHandlerService.java", "handleWordOfCommandCardChosen"): "Word of Command",
    ("service/effect/normalfx/CastTopOfLibraryWithoutPayingManaCostEffectHandler.java", "resolve"):
        "the cast-from-library offer",
    ("service/battlefield/AsEntersDiscardSupport.java", "begin"):
        "never asked: the carrier of the number choice \"how many cards to discard\"",
}

# (file suffix, method) -> why the entry its answer puts on the stack is a stack object by the rules.
CONSUMERS = {
    ("service/effect/mayfx/ReflectMayEffectHandler.java", "handle"):
        "reflect: \"When they do, they create a token copy\" is a reflexive triggered ability (CR 603.12)",
    ("service/effect/mayfx/MiracleRevealHandler.java", "handle"):
        "miracle: \"When you do, you may cast it\" is the keyword's triggered ability (CR 702.94a)",
    ("service/effect/mayfx/BecomeCopyOfOneOfCardsUntilEndOfTurnChoiceHandler.java", "handle"):
        "no entry: the record of the resolving entry is handed to the graveyard choice it begins (run 17z16c4)",
    ("service/input/MayCastHandlerService.java", "*"):
        "a spell cast as the answer (CR 601.2a): the spell is the stack object",
}


def read(path):
    with open(path, encoding="utf-8") as handle:
        return handle.read()


def handled_types():
    """The effect types a `MayEffectHandlerBean` is registered for, wherever the bean is, and the interfaces all of
    whose implementing effect records have one (a producer may declare its payload by the interface)."""
    handled = set()
    for directory, _, names in os.walk(os.path.join(SOURCES[0], "service")):
        for name in names:
            if not name.endswith(".java"):
                continue
            text = read(os.path.join(directory, name))
            if not re.search(r"implements[^{]*\bMayEffectHandlerBean\b", text):
                continue
            match = re.search(r"handledEffect\(\)\s*\{\s*return\s+([\w.]+)\.class", text)
            if match:
                handled.add(match.group(1).split(".")[-1])
    implementers = {}
    for directory, _, names in os.walk(os.path.join(SOURCES[1], "model")):
        for name in names:
            if not name.endswith(".java"):
                continue
            match = re.search(r"\b(?:record|class)\s+" + re.escape(name[:-5]) + r"\b[^{]*?\bimplements\s+([^{]+)\{",
                              read(os.path.join(directory, name)), re.S)
            if match:
                for interface in re.findall(r"[A-Z]\w+", re.sub(r"<[^>]*>", "", match.group(1))):
                    implementers.setdefault(interface, set()).add(name[:-5])
    for interface, types in implementers.items():
        if interface != "CardEffect" and types <= handled:
            handled.add(interface)
    return handled


def call_text(text, start):
    depth = 0
    index = text.index("(", start)
    while True:
        char = text[index]
        if char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return text[start:index + 1]
        index += 1


def arguments(call):
    inner = call[call.index("(") + 1:-1]
    parts, depth, current = [], 0, ""
    for char in inner:
        if char in "([{<":
            depth += 1
        elif char in ")]}>":
            depth -= 1
        if char == "," and depth == 0:
            parts.append(current.strip())
            current = ""
        else:
            current += char
    parts.append(current.strip())
    return parts


def payload_types(expression, text, position, home):
    """The effect types the payload expression names, and the identifiers it could not type."""
    types = [a or b for a, b in re.findall(r"new ([\w.]+)\(|\b([A-Z]\w+)\.\w+\(", expression)]
    types = [t.split(".")[-1] for t in types]
    unresolved = []
    stripped = re.sub(r"new [\w.]+\(", "(", expression)
    for identifier in sorted(set(re.findall(r"(?<![\w.])([a-z]\w*)\b(?!\s*\()", stripped))):
        if identifier in ("new", "null", "true", "false"):
            continue
        before = text[:position]
        name = re.escape(identifier)
        found = []
        for pattern in (r"\b([A-Z][\w.]*(?:<[^>;=]+>)?)\s+" + name + r"\s*(?:=|;|,|\)|:)",
                        r"instanceof\s+([\w.]+)\s+" + name + r"\b",
                        r"\b" + name + r"\s*=\s*\(([A-Z][\w.]*)\)",
                        r"\b" + name + r"\s*=\s*new ([A-Z][\w.]*)\("):
            matches = list(re.finditer(pattern, before))
            if matches:
                found.append(matches[-1])
        best = max(found, key=lambda m: m.start(), default=None)
        resolved = best.group(1).split(".")[-1] if best else None
        if resolved in (None, "CardEffect", "var") and home:
            resolved = home
        if resolved is None:
            unresolved.append(identifier)
        else:
            types.append(resolved)
    return [t for t in types if t not in NOT_EFFECTS and not t.startswith("List<")], unresolved


def enclosing_method(lines, line):
    for index in range(line, -1, -1):
        match = METHOD.match(lines[index])
        if match:
            return match.group(1), index
    return "?", 0


def scan():
    handled = handled_types()
    producers, consumers = [], []
    for base in SOURCES:
        for directory, _, names in os.walk(base):
            for name in sorted(names):
                if not name.endswith(".java") or name == "PendingMayAbility.java":
                    continue
                path = os.path.join(directory, name)
                text = read(path)
                lines = text.split("\n")
                suffix = path.split("magicalvibes/", 1)[1]
                home = None
                if name.endswith("EffectHandler.java"):
                    home = name[:-len("Handler.java")]
                elif name.endswith("Handler.java") and "/mayfx/" in path:
                    home = name[:-len("Handler.java")] + "Effect"
                for match in PRODUCER.finditer(text):
                    line = text.count("\n", 0, match.start())
                    if re.search(r"(?:public|private) \w+ queueMayAbilityForPlayer", lines[line]):
                        continue
                    method, method_line = enclosing_method(lines, line)
                    call = call_text(text, match.start())
                    args = arguments(call)
                    types, unresolved = payload_types(args[2], text, match.start(), home)
                    for fragment, read_types in PAYLOADS.get((suffix, method), []):
                        if fragment in args[2]:
                            types, unresolved = list(read_types), []
                    body = "\n".join(lines[method_line:line + 14])
                    cost = len(args) > 5 and args[5] not in ("null", "") and not suffix.endswith("GameData.java") \
                        and "String" not in args[5] and len(args) > 6
                    producers.append({
                        "file": suffix, "line": line + 1, "method": method, "types": types, "cost": cost,
                        "unresolved": unresolved, "flag": "resolvingMayEffectFromStack = true" in body,
                        "handled": sorted(set(t for t in types if t in handled)),
                    })
                if "/mayfx/" in path or re.search(r"service/input/May\w+\.java$", path):
                    for match in STACKED.finditer(text):
                        line = text.count("\n", 0, match.start())
                        method, _ = enclosing_method(lines, line)
                        consumers.append({"file": suffix, "line": line + 1, "method": method,
                                          "text": lines[line].strip()})
    return producers, consumers


def route_of(producer):
    key = (producer["file"], producer["method"])
    if producer["flag"]:
        return "RESOLUTION", "asked as the entry's step resolves"
    if key in SPECIAL_ROUTES:
        return "SPECIAL", SPECIAL_ROUTES[key]
    if producer["handled"]:
        return "HANDLER", ", ".join(producer["handled"])
    return "GENERIC", "carried out in the resolution that asked"


def verdict_of(producer):
    key = (producer["file"], producer["method"])
    if key in RULES:
        return RULES[key]
    if producer["file"].startswith(RESOLVING_DIRS):
        if producer["flag"]:
            return ON_STACK, "the question of the entry on the stack, asked as it resolves (CR 603.5)"
        return RESOLVING, "an optional action of the resolving spell or ability (CR 608.2d)"
    return "UNCLASSIFIED", "read the method, then convert it or explain it in RULES"


def consumer_reason(consumer):
    return CONSUMERS.get((consumer["file"], consumer["method"])) or CONSUMERS.get((consumer["file"], "*"))


def main():
    producers, consumers = scan()
    summary = "--summary" in sys.argv
    only_generic = "--generic" in sys.argv
    only_unclassified = "--unclassified" in sys.argv
    verdicts, routes, problems = {}, {}, 0
    for producer in sorted(producers, key=lambda p: (p["file"], p["line"])):
        verdict, reason = verdict_of(producer)
        if verdict is None:
            continue
        route, route_reason = route_of(producer)
        verdicts[verdict] = verdicts.get(verdict, 0) + 1
        routes[route] = routes.get(route, 0) + 1
        bad = verdict in ("UNCLASSIFIED", TRIGGER)
        problems += bad
        if summary or (only_generic and route != "GENERIC") or (only_unclassified and not bad):
            continue
        print(f"{producer['file']}:{producer['line']} {producer['method']}: "
              f"{', '.join(producer['types']) or '(no effect type named)'}"
              + (f" [untyped: {', '.join(producer['unresolved'])}]" if producer["unresolved"] else ""))
        print(f"    {verdict}: {reason}")
        print(f"    route {route}: {route_reason}")
    unexplained = 0
    if not only_generic:
        for consumer in sorted(consumers, key=lambda c: (c["file"], c["line"])):
            reason = consumer_reason(consumer)
            unexplained += reason is None
            if summary or (only_unclassified and reason is not None):
                continue
            print(f"consumer {consumer['file']}:{consumer['line']} {consumer['method']}: {consumer['text']}")
            print(f"    {reason or 'UNEXPLAINED: an accepted answer put on the stack'}")
    classes = {
        "(a) a triggered ability's \"you may\" asked before the ability is on the stack": [TRIGGER],
        "(b) a question that is not a triggered ability's resolution choice": [
            REPLACEMENT, AS_ENTERS, COST, PREGAME, TURN_BASED, AS_ATTACKS, DRAW, RESOLVING],
        "(c) on the stack, asked as the entry resolves": [ON_STACK],
    }
    print(f"producers: {sum(verdicts.values())}")
    for label, names in classes.items():
        print(f"  {label}: {sum(verdicts.get(name, 0) for name in names)}")
        for name in names:
            if verdicts.get(name):
                print(f"      {name}: {verdicts[name]}")
    if verdicts.get("UNCLASSIFIED"):
        print(f"  UNCLASSIFIED: {verdicts['UNCLASSIFIED']}")
    print("answer routes: " + ", ".join(f"{name} {count}" for name, count in sorted(routes.items())))
    print(f"consumers: {len(consumers)} stacked or routed entries in the answering code, {unexplained} unexplained")
    return 1 if problems or unexplained else 0


if __name__ == "__main__":
    sys.exit(main())
