#!/usr/bin/env python3
"""Scans every delayed-action queue of the engine and shows how each groups (effect-slot-refactor.md step 15, run 17z17d).

Read-only. A delayed triggered ability is created by one resolution (CR 603.7a), so each is a stack object of its
own; one resolution's "exile them at the beginning of the next end step" is one ability for all of them. The scan
lists every place a queue of delayed actions is drained or read toward the stack and says how the stack objects are
grouped there:

* the DelayedAction queue (GameData.delayedActions): every drainDelayedActions, drainFirstDelayedAction,
  peekFirstDelayedAction or getDelayedActions call for a type, and every "instanceof" of a DelayedAction type;
* the "when you next cast / activate" counters and lists of GameData (pendingNext...ThisTurn...) and the
  copy-rider mana (takeSpentCopyGrantingMana);
* the source-linked maps (exileReturnOnPermanentLeave, exiledCardReturnToHandOnPermanentLeave).

A site is the text from a queue's first read in a method (and the two lines before it, for a loop header that
holds the read) to the next queue's first read or the method's end; it is classified:

    EACH        builds its stack entries inside a loop over the drained actions, one per action (or the action is a
                record of one resolution)
    GROUPED     collects the actions under a key first (computeIfAbsent, groupingBy, merge, toMap): the key decides
    NO ENTRY    builds no stack entry in the method (the action is carried out directly, re-queued or handed on)
    ONE ENTRY   builds a stack entry outside any loop

A GROUPED or ONE ENTRY site needs a verdict in VERDICTS, or it is printed UNEXPLAINED and the script exits 1. A
verdict that begins with OPEN is a site an open row owns; it is printed OPEN whatever its text looks like (the
counters of checkSpellCastTriggers are read at the top of the method and turned into entries further down, past
other queues' reads, so their own text shows no entry).

    scripts/step15-run17z17d-scan.py            every site, one per line
    scripts/step15-run17z17d-scan.py --summary  the counts
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MAIN = ROOT / "magical-vibes-engine/src/main/java"
DOMAIN = ROOT / "magical-vibes-domain/src/main/java"

ACTIONS = sorted(path.stem for path in (DOMAIN / "com/github/laxika/magicalvibes/model/action").glob("*.java")
                 if path.stem not in ("DelayedAction", "DelayedPermanentActionKind"))
DRAIN = re.compile(r"(?:drainDelayedActions|drainFirstDelayedAction|peekFirstDelayedAction|getDelayedActions)"
                   r"\(\s*(?:[a-z.]+\.)?([A-Za-z]+)\.class|instanceof (?:[a-z.]+\.)?(" + "|".join(ACTIONS) + r")\b")
COUNTER = re.compile(r"gameData\.(pendingNext[A-Za-z]*ThisTurn[A-Za-z]*)\.(get|getOrDefault|remove)\(")
RIDER = re.compile(r"takeSpentCopyGrantingMana\(")
LINKED = re.compile(r"gameData\.(exileReturnOnPermanentLeave|exiledCardReturnToHandOnPermanentLeave"
                    r"|chosenSubtypeActivationCopiesThisTurn)\b")
METHOD = re.compile(r"^    (?! )(?:public |private |protected )?(?:static )?(?:<[^>]+> )?[\w<>\[\], .?]+ (\w+)\([^;]*$")
GROUPING = re.compile(r"computeIfAbsent\(|groupingBy\(|\.merge\(|toMap\(")
LOOP = re.compile(r"^\s*(for \(|while \(|.*\.forEach\()")
ENTRY = re.compile(r"new StackEntry\((?:\s*$|StackEntryType)")

# (file name, method, queue) -> how the site's stack objects are grouped and why that is one object per delayed
# ability.
VERDICTS = {
    ("PermanentRemovalService.java", "processDelayedPermanentActions", "DelayedPermanentAction"):
        "RESOLUTION: the key is the source card, the controller, the sacrificing player and "
        "DelayedPermanentAction.schedulingResolution, so one resolution's permanents are one ability and two "
        "resolutions of one source are two (Molten Echoes); a kind outside the six batched ones, or an action "
        "with no source card, is a key of its own",
    ("StepTriggerService.java", "queueDelayedPermanentExiles", "DelayedPermanentAction"):
        "RESOLUTION: the key is the source card, the controller and DelayedPermanentAction.schedulingResolution "
        "(Storm Herald's Auras one ability; Corpse Dance cast twice with buyback two); a lone action is an entry "
        "of its own",
    ("StepTriggerService.java", "handleUpkeepTriggers", "ExilePermanentAtNextUpkeep"):
        "RESOLUTION: the key is the source card, the controller and the action's schedulingResolution (Rally the "
        "Ancestors' creatures one ability)",
    ("StepTriggerService.java", "handleUpkeepTriggers", "ExileToOwnerGraveyardAtNextUpkeep"):
        "RESOLUTION: the key is the source card, the controller and the action's schedulingResolution (Three "
        "Wishes' cards one ability)",
    ("StepTriggerService.java", "handleUpkeepTriggers", "ReboundAtNextUpkeep"):
        "EACH: one rebound ability per action in the loop; the computeIfAbsent is a later statement of the method "
        "(untappedLandsAtTurnStart), the last site's text runs to the method's end",
    ("StepTriggerService.java", "handleEndStepTriggers", "ExileToOwnerGraveyardAtNextEndStep"):
        "RESOLUTION: the key is the action's groupId, a UUID the scheduling resolution made for its cards, or the "
        "card itself for a lone one",
    ("TurnProgressionService.java", "advanceTurn", "LoseGameAtEndStep"):
        "NO ENTRY: the actions of a skipped extra turn are dropped; the computeIfAbsent is a later statement of "
        "the method",
    ("TurnProgressionService.java", "processAdditionalCombatBeginningEffects",
     "DelayedAdditionalCombatBeginningEffect"):
        "EACH: drainFirstDelayedAction takes one action per additional combat and builds one entry for it",
    ("CombatBlockService.java", "processBlockerDeclaration", "DelayedBlockerDeclarationControl"):
        "NO ENTRY: read for the source card of a payment question; the grouping is the block-tax map of the "
        "declaration, not a stack object",
    ("PredicateEvaluationService.java", "matchesCardPredicateInternal", "DelayedControllerSpellCastTrigger"):
        "NO ENTRY: a predicate reads the pending trigger's source; no stack object is built",
    ("GameData.java", "simulationFrameCopy", "DelayedControllerSpellCastTrigger"):
        "NO ENTRY: the simulation copy of the queue",
    ("TriggerCollectionService.java", "checkSpellCastTriggers", "pendingNextInstantSorceryCopyThisTurnCount"):
        "OPEN (row 17z17d2): the count is the sum of every pending \"copy the next instant or sorcery spell\" "
        "delayed ability's copies. The one-copy abilities counted in "
        "pendingNextInstantSorceryCopyThisTurnSingleTriggerCount are an entry each; what is left is put on the "
        "stack as one entry of that many copies, so two resolutions of a \"copy it twice\" or \"copy it X "
        "times\" (CopyNextInstantOrSorceryCastThisTurnForXValueEffectHandler) and the +1 of "
        "CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffectHandler are merged: the counters keep no "
        "per-resolution breakdown",
    ("TriggerCollectionService.java", "checkSpellCastTriggers",
     "pendingNextInstantSorceryCopyThisTurnDynamicSources"):
        "EACH: one entry per pending dynamic-count ability in the loop; the entry outside the loop after it is "
        "the merged multi-copy entry of pendingNextInstantSorceryCopyThisTurnCount (row 17z17d2), whose own "
        "first read is at the top of the method",
    ("TriggerCollectionService.java", "checkSpellCastTriggers",
     "pendingNextInstantSorceryCopyThisTurnSingleTriggerCount"):
        "OPEN (row 17z17d2): the entries outside the loop are the merged multi-copy entry of "
        "pendingNextInstantSorceryCopyThisTurnCount and the merged entry of the mana-value-limited copies; the "
        "one-copy abilities are an entry each in the loop between them",
    ("TriggerCollectionService.java", "checkSpellCastTriggers", "pendingNextInstantSorceryCopyThisTurnMaxManaValues"):
        "OPEN (row 17z17d2): each limit the spell matches is a copy, summed into one entry (limitedCopies). A "
        "kicked Sea Gate Stormcaller's two limits are one delayed ability, two Stormcallers' limits are two; the "
        "list keeps no resolution to tell them apart",
    ("PermanentRemovalService.java", "handleExileReturnOnLeave", "exiledCardReturnToHandOnPermanentLeave"):
        "EACH: a permanent has at most one such return (a map value), one entry",
    ("PermanentRemovalService.java", "handleExileReturnOnLeave", "exileReturnOnPermanentLeave"):
        "PRINTED: the one entry outside a loop is the source's own printed leaves-the-battlefield ability "
        "returning every card exiled with it (Fiend Hunter, Parallax Wave), one ability by its text; the delayed "
        "returns each exile created (Portcullis, haunt) are built one per pending return in loops. Tawnos's Coffin "
        "is no longer here: its returns are DelayedSourceUntapsOrLeavesTrigger actions, one per resolution",
}

def methods(path):
    """(name, first line, last line, text) of each method of the file, by indentation."""
    lines = path.read_text(encoding="utf-8").split("\n")
    found = []
    start = None
    name = None
    for number, line in enumerate(lines):
        match = METHOD.match(line)
        if match and not line.strip().startswith(("return", "new", "throw", "else")):
            start, name = number, match.group(1)
        elif line == "    }" and start is not None:
            found.append((name, start, number, lines[start:number + 1]))
            start = None
    return found


def classify(body):
    """How the method's stack entries relate to its loops."""
    if GROUPING.search("\n".join(body)):
        return "GROUPED"
    depth_of_loops = []
    in_loop = False
    outside = False
    for line in body:
        indent = len(line) - len(line.lstrip())
        depth_of_loops = [depth for depth in depth_of_loops if indent > depth or not line.strip()]
        if LOOP.match(line):
            depth_of_loops.append(indent)
        if ENTRY.search(line):
            if depth_of_loops:
                in_loop = True
            else:
                outside = True
    if in_loop and not outside:
        return "EACH"
    if outside:
        return "ONE ENTRY"
    return "NO ENTRY"


def sites():
    """(file, method, line, queue, classification) for every read of a delayed queue."""
    found = []
    for path in sorted(MAIN.rglob("*.java")) + sorted(DOMAIN.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        if not (DRAIN.search(text) or COUNTER.search(text) or RIDER.search(text) or LINKED.search(text)):
            continue
        for name, start, end, body in methods(path):
            queues = []
            joined = "\n".join(body)
            for pattern, label in ((DRAIN, None), (COUNTER, None), (LINKED, None), (RIDER, "copy-rider mana")):
                for match in pattern.finditer(joined):
                    queues.append((joined.count("\n", 0, match.start()),
                                   label or next(group for group in match.groups() if group)))
            queues.sort()
            # A site is the text from one queue's first read to the next queue's first read (or the method's end).
            firsts = []
            seen = set()
            for offset, queue in queues:
                if queue not in seen:
                    seen.add(queue)
                    firsts.append((offset, queue))
            for index, (offset, queue) in enumerate(firsts):
                until = firsts[index + 1][0] if index + 1 < len(firsts) else len(body)
                # Two lines before the read, for a loop header that holds it.
                block = body[max(0, offset - 2):max(until, offset + 1)]
                found.append((path.name, name, start + offset + 1, queue, classify(block)))
    return found


def main():
    found = sites()
    counts = {}
    unexplained = 0
    lines = []
    for file, method, line, queue, kind in found:
        verdict = VERDICTS.get((file, method, queue))
        if kind in ("GROUPED", "ONE ENTRY") and verdict is None:
            kind_shown = f"UNEXPLAINED {kind}"
            unexplained += 1
        elif verdict is not None and verdict.startswith("OPEN"):
            kind_shown = "OPEN"
        else:
            kind_shown = kind
        counts[kind_shown] = counts.get(kind_shown, 0) + 1
        lines.append(f"{file}:{line}\t{method}\t{queue}\t{kind_shown}" + (f"\t{verdict}" if verdict else ""))
    if "--summary" not in sys.argv:
        print("\n".join(lines))
    queues = {queue for _, _, _, queue, _ in found}
    unread = [action for action in ACTIONS if action not in queues]
    if unread:
        print("DelayedAction types no site reads: " + ", ".join(unread), file=sys.stderr)
    print(f"{len(found)} sites on {len(queues)} queues in {len({file for file, *_ in found})} files: "
          + ", ".join(f"{count} {kind}" for kind, count in sorted(counts.items())), file=sys.stderr)
    sys.exit(1 if unexplained else 0)


if __name__ == "__main__":
    main()
