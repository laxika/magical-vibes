#!/usr/bin/env python3
"""Step 15, run 17z16c: the reflexive and delayed triggered abilities effect and choice handlers build (read-only).

Lists, for every method under the engine's service/effect/** and service/input/** packages (and the few other
services row 17z16c names) that puts a TRIGGERED_ABILITY entry on the stack or in a holding queue, or queues or
asks a trigger choice:

  STACK   a `new StackEntry(StackEntryType.TRIGGERED_ABILITY, ...)` that reaches `gameData.stack.add` /
          `enqueueTrigger` (by its variable, or inline)
  CHOICE  a trigger choice queued (`queueInteraction(new ...)`) or asked (`setPermanentChoiceContext(new ...)`)
  copies  what the method copies from the resolving entry by hand (`x.setFoo(entry.getFoo())`)
  record  whether it uses the shared record (`ReflexiveAbilitySupport`, `copyTriggerEventFrom`,
          `carryTriggerRecord`, `handOffTriggerRecord`), in the method itself or in the method of the same file that
          routes it (the summary counts a file as using it when any of its methods does). A method that names no
          record and was read by hand in run 17z16c2 prints why that is right instead of `NONE` (`EXPLAINED`
          below): `through the hand` (its choice was begun with a record handed off, and the hand gives it to the
          entry the method builds) or `none to record` (an ability of its own event with one route). An
          explanation whose method no longer builds an entry or choice is reported as stale.
  class   `instanceof ...Effect` tests in the method (a path or a record picked by an effect's class)
  rebuild `new ...Effect(` constructions in the method (an effect rebuilt with data bound in)

Usage: step15-run17z16c-scan.py [--all] [--summary]
  --all      also list scratch entries (constructed, never stacked)
  --summary  one line per file
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SERVICE = os.path.join(ROOT, "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service")
PACKAGES = ["effect", "input"]
EXTRA = [
    "DamagePreventionService.java",
    "ExileCastTargetSupport.java",
    "CloneService.java",
]

METHOD = re.compile(
    r"^    (?! )(?!(?:if|for|while|switch|else|try|catch|do|return|synchronized|new|throw)\b)"
    r"(?:@\w+ +)*(?:(?:public|private|protected|static|final|synchronized) +)*[\w<>\[\], .?]+ +(\w+) *\(")
NEW_ENTRY = re.compile(r"(?:(\w+)\s*=\s*)?new StackEntry\(")
TRIGGER_CHOICE = re.compile(
    r"(queueInteraction(?:First)?|setPermanentChoiceContext|beginInteraction)\(\s*new ([\w.]+)\(")
COPY = re.compile(r"(\w+)\.set(\w+)\((\w+)\.(?:get|is)(\w+)\(\)\)")
RECORD = re.compile(r"ReflexiveAbilitySupport|reflexiveAbilitySupport|copyTriggerEventFrom|carryTriggerRecord|handOffTriggerRecord")
INSTANCEOF = re.compile(r"instanceof (\w+Effect)\b")
REBUILD = re.compile(r"new (\w+Effect)\(")
TRIGGER_CHOICES = (
    "AttackTriggerTarget", "ETBTokenMultiTargetTrigger", "SpellGraveyardTargetTrigger", "MayAbilityTriggerTarget",
    "SpellTargetTriggerAnyTarget", "EntersTriggerTarget", "TriggeredModalTrigger", "ETBSpellTargetTrigger",
    "DeathTriggerTarget", "SelfTriggeredAbilityTarget", "DiscardTriggerAnyTarget", "ReflexiveTriggerTarget",
    "ChampionedTriggerTarget", "EarthbendThenFightTarget",
)
HAND = "through the hand"
OWN = "none to record"
EXPLAINED = {
    ("effect/normalfx/AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffectHandler.java", "resolve"):
        (OWN, "the scheme set in motion again is another card's ability with an event of its own; one route"),
    ("effect/normalfx/EarthbendTargetLandThenFightEffectHandler.java", "handleTargetChoice"):
        (HAND, "resolve() routes the resolving entry's record around both target prompts"),
    ("effect/normalfx/EvolveEffectHandler.java", "fireEvolvesTriggers"):
        (OWN, "'whenever this creature evolves' (CR 702.100b) is the creature's own ability; one route, no event data"),
    ("effect/normalfx/ExplosionOfRichesEffectHandler.java", "queueDamageTriggers"):
        (OWN, "'whenever a card is drawn this way': one route (a random opponent), its event has no data"),
    ("effect/normalfx/RemoveCounterAndSacrificeSelfOnLastEffectHandler.java", "resolve"):
        (OWN, "vanishing's 'when the last time counter is removed' (CR 702.63a) is the permanent's own ability; one route"),
    ("effect/normalfx/RemoveRefineCounterFromExiledCardEffectHandler.java", "resolve"):
        (OWN, "the exiled card's own cast ability; one route, the card is its target"),
    ("effect/normalfx/RemoveTimeCounterFromExiledCardEffectHandler.java", "queueCastTrigger"):
        (OWN, "suspend's 'when the last time counter is removed' (CR 702.62a) is the exiled card's own ability; one route"),
    ("effect/normalfx/SpecializeGutEffectHandler.java", "resolve"):
        (OWN, "the specialized face's 'when this creature specializes'; one route, no event data"),
    ("effect/normalfx/SpecializeKarlachEffectHandler.java", "resolve"):
        (OWN, "the specialized card's return from the graveyard; one route, no event data"),
    ("effect/normalfx/SpecializeKlementEffectHandler.java", "enqueueSpecializationTrigger"):
        (OWN, "the specialized face's 'when this creature specializes'; one route, no event data"),
    ("effect/normalfx/SpecializeLukaminaEffectHandler.java", "queueSpecializationTrigger"):
        (OWN, "the untargeted faces' 'when this creature specializes'; one route (the targeted face routes its own)"),
    ("effect/normalfx/SpecializeSarevokEffectHandler.java", "enqueueSpecializationTrigger"):
        (OWN, "the specialized face's 'when this creature specializes'; one route, no event data"),
    ("effect/normalfx/SpecializeVhalEffectHandler.java", "enqueueNonTargetingTrigger"):
        (OWN, "the untargeted faces' ability with the counters removed as its event value, as the routed record has"),
    ("effect/normalfx/SpecializeWyllEffectHandler.java", "resolve"):
        (OWN, "the specialized face's 'when this creature specializes'; one route, no event data"),
    ("effect/normalfx/TakeInitiativeEffectHandler.java", "resolve"):
        (OWN, "'whenever a player takes the initiative' (CR 727.2) has no source and no event data; one route"),
    ("effect/normalfx/UnattachTriggerSupport.java", "triggerDestroyOnUnattachIfNeeded"):
        (OWN, "a collector: the entry it builds is the record (the creature it left as triggering permanent); one route"),
    ("effect/mayfx/AttachTargetAuraOrEquipmentToTargetCreatureHandler.java", "handle"):
        (HAND, "the walk sets the targets of the entry still resolving, which asked the question; no entry is built"),
    ("effect/mayfx/AttachTargetEquipmentToTargetCreatureHandler.java", "handle"):
        (HAND, "the walk sets the targets of the entry still resolving, which asked the question; no entry is built"),
    ("effect/mayfx/MiracleRevealHandler.java", "handle"):
        (OWN, "miracle's linked ability (CR 702.94a) of the revealed card; one route, no event data"),
    ("effect/manafx/ReflexiveManaAbilityRiderEffectHandler.java", "resolve"):
        (OWN, "a mana ability's \"when you do\", written by the card: no entry resolves; one route"),
    ("input/ChoiceHandlerService.java", "handleMultipleChooseModeChoice"):
        (HAND, "resumePendingMayResolution: the walk sets the targets of the entry still resolving, no entry is built"),
    ("input/ChoiceHandlerService.java", "handleSagaChapterCounterAssignment"):
        (HAND, "the chapter collectors hand off chapterEntry before the target and counter prompts"),
    ("input/GraveyardChoiceHandlerService.java", "handleGraveyardCardChosen"):
        (HAND, "MAY_ABILITY_TARGET and COPY_FROM_LEAVING_GRAVEYARD prompts are begun inside a route"),
    ("input/MayAbilityHandlerService.java", "handleResolutionTimeTargetSelection"):
        (HAND, "resolvedMayTargetingEntry: the target is set on the entry still resolving, no entry is built"),
    ("input/MultiPermanentChoiceHandlerService.java", "handleSagaChapterTargetSelection"):
        (HAND, "the chapter collectors hand off chapterEntry before the target prompt"),
    ("input/MultiPermanentChoiceHandlerService.java", "handleCapriciousEfreetOpponentTargets"):
        (HAND, "the upkeep collector hands off; PendingCapriciousEfreetState is queued with the record and polled here"),
    ("input/PermanentChoiceTriggerHandlerService.java", "handleTransformTriggerTarget"):
        (HAND, "AnimationSupport hands off the transform ability's entry before the target prompt"),
    ("input/PermanentChoiceTriggerHandlerService.java", "handleSagaChapterTarget"):
        (HAND, "the chapter collectors hand off chapterEntry before the target prompt"),
    ("DamagePreventionService.java", "preventDamageAndSacrificeAttachedEquipment"):
        (OWN, "'when damage is prevented this way': no entry resolves; one route, no event data read"),
    ("DamagePreventionService.java", "queuePreventedDamageLifeGainTrigger"):
        (OWN, "'whenever damage is prevented this way': the entry records the damage prevented as its event value; one route"),
    ("DamagePreventionService.java", "queuePreventedDamageReflexiveTrigger"):
        (OWN, "'when damage is prevented this way': the entry records the damage prevented as its event value; one route"),
    ("battlefield/CloneService.java", "completeCloneEntry"):
        (OWN, "abilities made as the copy enters: no entry resolves; one route each, the card to exile is its triggering card"),
}


def java_files():
    for package in PACKAGES:
        for directory, _, names in os.walk(os.path.join(SERVICE, package)):
            for name in sorted(names):
                if name.endswith(".java"):
                    yield os.path.join(directory, name)
    for directory, _, names in os.walk(SERVICE):
        if any(os.sep + p in directory[len(SERVICE):] for p in PACKAGES):
            continue
        for name in sorted(names):
            if name in EXTRA:
                yield os.path.join(directory, name)


def methods(lines):
    """Yields (name, start, end) of each member method: a four-space-indented declaration up to its closing brace."""
    i = 0
    while i < len(lines):
        match = METHOD.match(lines[i])
        if not match:
            i += 1
            continue
        start = i
        j = i
        while j < len(lines) and not lines[j].rstrip().endswith("{") and ";" not in lines[j]:
            j += 1
        if j >= len(lines) or not lines[j].rstrip().endswith("{"):
            i = j + 1
            continue
        depth = 0
        end = len(lines) - 1
        for k in range(j, len(lines)):
            depth += lines[k].count("{") - lines[k].count("}")
            if depth <= 0:
                end = k
                break
        yield match.group(1), start, end
        i = end + 1


def statement(lines, index, limit):
    text = lines[index]
    depth = text.count("(") - text.count(")")
    k = index
    while depth > 0 and k + 1 <= limit:
        k += 1
        text += lines[k]
        depth += lines[k].count("(") - lines[k].count(")")
    return text


def scan(path, show_all):
    with open(path, encoding="utf-8") as handle:
        lines = handle.readlines()
    found = []
    bodies = {name: "".join(lines[start:end + 1]) for name, start, end in methods(lines)}
    routers = [body for body in bodies.values() if "ReflexiveAbilitySupport.route(" in body]
    routed = {name for name in bodies
              if any(re.search(r"\b" + re.escape(name) + r"\(", body[body.index("ReflexiveAbilitySupport.route("):])
                     for body in routers)}
    grew = True
    while grew:
        grew = False
        for name in bodies:
            if name not in routed and any(re.search(r"\b" + re.escape(name) + r"\(", bodies[caller])
                                          for caller in routed if caller != name):
                routed.add(name)
                grew = True
    for name, start, end in methods(lines):
        body = "".join(lines[start:end + 1])
        sites = []
        for index in range(start, end + 1):
            match = NEW_ENTRY.search(lines[index])
            if match:
                text = statement(lines, index, end)
                if "StackEntryType.TRIGGERED_ABILITY" not in text:
                    continue
                variable = match.group(1)
                inline = re.search(r"(stack\.add\w*|enqueueTrigger\w*)\(\s*new StackEntry\(", text) is not None
                stacked = inline
                if variable and not stacked:
                    stacked = re.search(
                        r"(stack\.add\w*|enqueueTrigger\w*|putOnStack\w*|handOffTriggerRecord|stackReflexive\w*|route\w*)\([^;]*\b"
                        + re.escape(variable) + r"\b", body) is not None
                if stacked or show_all:
                    sites.append((index + 1, "STACK" if stacked else "scratch", variable or "(inline)"))
            match = TRIGGER_CHOICE.search(statement(lines, index, end)) if "new " in lines[index] or \
                TRIGGER_CHOICE.search(lines[index]) else None
            if match is None and re.search(r"(queueInteraction(?:First)?|setPermanentChoiceContext)\(\s*$", lines[index]):
                match = TRIGGER_CHOICE.search(statement(lines, index, end))
            if match and match.group(2).split(".")[-1] in TRIGGER_CHOICES:
                sites.append((index + 1, "CHOICE", match.group(2).split(".")[-1]))
        if not any(kind != "scratch" for _, kind, _ in sites) and not (show_all and sites):
            continue
        copies = sorted({m.group(2) for m in COPY.finditer(body) if m.group(2) == m.group(4)})
        found.append({
            "file": os.path.relpath(path, SERVICE),
            "file_record": name in routed,
            "method": name,
            "line": start + 1,
            "sites": sites,
            "copies": copies,
            "record": sorted(set(RECORD.findall(body))),
            "class": sorted(set(INSTANCEOF.findall(body))),
            "rebuild": sorted(set(REBUILD.findall(body))),
        })
    return found


def record_text(method):
    if method["record"]:
        return ", ".join(method["record"])
    if method["file_record"]:
        return "in the method that routes this one"
    explained = EXPLAINED.get((method["file"], method["method"]))
    return "%s (%s)" % explained if explained else "NONE"


def main():
    show_all = "--all" in sys.argv
    summary = "--summary" in sys.argv
    files = stacked = choices = with_record = class_keyed = rebuilds = 0
    unexplained = by_hand = no_record = 0
    seen = set()
    for path in java_files():
        found = scan(path, show_all)
        if not found:
            continue
        files += 1
        relative = os.path.relpath(path, SERVICE)
        for method in found:
            if not method["record"] and not method["file_record"]:
                kind = EXPLAINED.get((method["file"], method["method"]), (None,))[0]
                seen.add((method["file"], method["method"]))
                unexplained += kind is None
                by_hand += kind == HAND
                no_record += kind == OWN
        file_sites = [site for method in found for site in method["sites"]]
        stacked += sum(1 for _, kind, _ in file_sites if kind == "STACK")
        choices += sum(1 for _, kind, _ in file_sites if kind == "CHOICE")
        uses_record = any(method["record"] or method["file_record"] for method in found)
        with_record += uses_record
        class_keyed += any(method["class"] for method in found)
        rebuilds += any(method["rebuild"] for method in found)
        if summary:
            print("%-90s stack=%d choice=%d record=%s class=%d rebuild=%d" % (
                relative,
                sum(1 for _, kind, _ in file_sites if kind == "STACK"),
                sum(1 for _, kind, _ in file_sites if kind == "CHOICE"),
                "yes" if uses_record else "NO",
                sum(len(method["class"]) for method in found),
                sum(len(method["rebuild"]) for method in found)))
            continue
        print(relative)
        for method in found:
            print("  %s (line %d)" % (method["method"], method["line"]))
            for line, kind, what in method["sites"]:
                print("    %-7s %5d  %s" % (kind, line, what))
            print("    copies : %s" % (", ".join(method["copies"]) or "-"))
            print("    record : %s" % record_text(method))
            if method["class"]:
                print("    class  : %s" % ", ".join(method["class"]))
            if method["rebuild"]:
                print("    rebuild: %s" % ", ".join(method["rebuild"]))
    print()
    print("%d files; %d stacked entries, %d trigger choices; %d files use the shared record, %d don't; "
          "%d files test an effect's class in such a method, %d construct an effect there"
          % (files, stacked, choices, with_record, files - with_record, class_keyed, rebuilds))
    print("methods that name no record: %d unexplained, %d through the hand, %d with none to record"
          % (unexplained, by_hand, no_record))
    for key in sorted(set(EXPLAINED) - seen):
        print("stale explanation (the method names a record or builds no entry or choice now): %s %s" % key)


if __name__ == "__main__":
    main()
