#!/usr/bin/env python3
"""Step 15, run 17z12d: the discard collector records the discard on every entry it puts on the stack, whatever the
effects are (`DiscardTriggerCollectorService.recordDiscard`), and its class-keyed handlers that did what the default
does, or bound the discarding player or card for some classes only, are gone. The "you discard one or more cards"
watch gets a default of its own."""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/DiscardTriggerCollectorService.java"

DELETE = [
    "handleConditionalDiscard", "handleSelfUntapOnDiscard", "handleDiscardMay", "handleDiscardEventMay",
    "handleCastDiscardedCard", "handleDamageToDiscardingPlayer", "handleDamageToEachOpponentOnDiscard",
    "handleDamageToEachOpponentOnDiscardEvent", "handleDamageToAnyTargetOnDiscardEvent",
    "handleLifeLossToEachOpponentOnDiscardEvent", "handleDrawOnDiscardEvent", "handlePutCountersOnSelfOnDiscardEvent",
    "handleSeekCardSharingTypeOnDiscardEvent", "handleExileDiscardedFromGraveyard",
    "handleExileDiscardedFromGraveyardOnDiscardEvent", "handleExileTopCardMayPlayOnDiscard", "handleScryOnDiscard",
    "handleMillOnDiscard", "handlePerpetualBoostOnDiscard", "handleSequenceOnDiscardEvent",
    "handlePutCountersOnDiscard", "handlePutCountersOnOpponentDiscard", "handlePutCountersOnSourceOnDiscard",
    "handlePutCountersOnSelfOnDiscard", "handleCreateTokenOnDiscard", "handleCreateTokenOnControllerDiscard",
    "handleCreateTokenOnDiscardEvent", "handleExileTopCardsMayPlayUntilNextEndStepOnDiscardEvent",
    "handleAwardManaOnDiscard", "handleDrawOnDiscard", "handleLifeLossOnDiscard", "enqueueDiscardTrigger",
]


def method_span(src, name, start_at=0):
    """The span of the first method named `name` from `start_at`, with its Javadoc, comments and annotations."""
    m = re.search(r"\n    (?:private|public) [\w<>\[\], ]+ " + name + r"\(", src[start_at:])
    if not m:
        return None
    sig = start_at + m.start() + 1
    # Back up over annotations, Javadoc and comment lines directly above.
    begin = sig
    while True:
        prev_end = src.rfind("\n", 0, begin - 1)
        line = src[prev_end + 1:begin - 1] if prev_end >= 0 else ""
        stripped = line.strip()
        if stripped.startswith(("@", "/**", "*", "*/", "//")) or (stripped.startswith(("watch =", "})"))):
            begin = prev_end + 1
            continue
        if stripped.endswith(",") and not stripped.endswith(");"):
            begin = prev_end + 1
            continue
        break
    end = src.index("\n    }\n", sig) + len("\n    }\n")
    if src[end:end + 1] == "\n":
        end += 1
    return begin, end


src = PATH.read_text()
for name in DELETE:
    removed = 0
    while True:
        span = method_span(src, name)
        if span is None:
            break
        src = src[:span[0]] + src[span[1]:]
        removed += 1
    if removed == 0:
        sys.exit(f"no method {name}")

# Every entry the collector builds records the discard.
src = src.replace(
    """        entry.setTriggeringPlayerId(discard.discardingPlayerId());
        if (discard.discardedCard() != null) {
            entry.setTriggeringCards(List.of(discard.discardedCard()));
        }
        gameData.enqueueTrigger(entry);""",
    """        gameData.enqueueTrigger(recordDiscard(gameData, entry, ctx));""")
src = src.replace(
    """        if (ctx instanceof TriggerContext.Discard discard && discard.discardedCard() != null) {
            UUID triggeringCardId = discard.discardedCard().getId();
            entry.setTriggeringCardId(triggeringCardId);
            entry.setTriggeringCardGraveyardEntryVersion(gameData.graveyardEntryVersion(triggeringCardId));
        }
""", "")
src = src.replace(
    """        } else {
            gameData.enqueueTrigger(entry);
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard/cycle (may pay {})",""",
    """        } else {
            gameData.enqueueTrigger(recordDiscard(gameData, entry, ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on discard/cycle (may pay {})",""")
# Inline `gameData.enqueueTrigger(new StackEntry(...));` of the kept discard-watch handlers, and the cycling and
# opponent-caused fallbacks.
for name in ["handleSelfBoostOnDiscard", "handleSequenceOnDiscard", "handleGrantKeywordOnDiscard",
             "collectCycling", "collectOpponentCausedDiscard"]:
    span = method_span(src, name)
    body = src[span[0]:span[1]]
    new_body, n = re.subn(r"gameData\.enqueueTrigger\(new StackEntry\(((?:.|\n)*?)match\.permanent\(\)\.getId\(\)\)\);",
                          r"gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(\1match.permanent().getId()), ctx));",
                          body)
    if n != 1:
        sys.exit(f"{name}: {n} enqueues")
    src = src[:span[0]] + new_body + src[span[1]:]
src = src.replace("public boolean collectCycling(TriggerMatchContext match, CardEffect effect) {",
                  "public boolean collectCycling(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {")
src = src.replace("public boolean collectOpponentCausedDiscard(TriggerMatchContext match, CardEffect effect) {",
                  "public boolean collectOpponentCausedDiscard(TriggerMatchContext match, CardEffect effect,\n"
                  "                                                TriggerContext ctx) {")

default_doc_old = src[src.index("    /**\n     * \"Whenever you discard a card, …\" and \"whenever an opponent discards a card, …\""):
                      src.index("    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARDS)")]
src = src.replace(default_doc_old, """    /**
     * "Whenever you discard a card, …" and "whenever an opponent discards a card, …", for an untargeted effect no handler
     * of its own collects: the ability goes on the stack as written, one object of its effect (CR 603.3); a modal one
     * announces its mode there (CR 603.3c), among those its restriction leaves ("choose one that hasn't been chosen this
     * turn": Monument to Endurance). The entry records the discard ({@link #recordDiscard}). A targeted effect needs a
     * handler that chooses its target as the ability goes on the stack (CR 603.3d).
     */
""")

default_event = '''
    /**
     * "Whenever you discard one or more cards, …": the ability goes on the stack as written, once for the discard event
     * (CR 603.2c), recording it ({@link #recordDiscard}); a targeted one has its target chosen as it goes on the stack
     * (CR 603.3d), with the number of cards discarded as the choice's event value.
     */
    @CollectsLibraryTrigger(value = CardEffect.class, watch = LibraryWatch.ON_CONTROLLER_DISCARD_EVENT)
    private boolean handleDiscardEventDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        if (!(ctx instanceof TriggerContext.DiscardEvent discardEvent)) {
            return false;
        }
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.DiscardControllerTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId(),
                    discardEvent.discardedCount()));
        } else {
            gameData.enqueueTrigger(recordDiscard(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    null,
                    match.permanent().getId()), ctx));
        }
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on a discard event ({})", gameData.id, sourceCard.getName(),
                effect.getClass().getSimpleName());
        return true;
    }

    /**
     * Records the discard on {@code entry}, whatever its effects are: the discarding player as its triggering player
     * and, for an ability that chose no target, as its non-targeting target ("that player"); the cards discarded as
     * its triggering cards, each with the graveyard stay it went to ("those cards", "them": a card that left the
     * graveyard and returned is a new object, CR 400.7), a lone card also as its triggering card ("that card", "it");
     * and for "one or more cards" their number as its event value. An event of another kind records nothing.
     */
    static StackEntry recordDiscard(GameData gameData, StackEntry entry, TriggerContext ctx) {
        UUID discardingPlayerId;
        List<Card> discardedCards;
        if (ctx instanceof TriggerContext.Discard discard) {
            discardingPlayerId = discard.discardingPlayerId();
            discardedCards = discard.discardedCard() == null ? List.of() : List.of(discard.discardedCard());
        } else if (ctx instanceof TriggerContext.DiscardEvent discardEvent) {
            discardingPlayerId = discardEvent.discardingPlayerId();
            discardedCards = discardEvent.discardedCards();
            entry.setEventValue(discardEvent.discardedCount());
        } else {
            return entry;
        }
        entry.setTriggeringPlayerId(discardingPlayerId);
        if (entry.getTargetId() == null && discardingPlayerId != null) {
            entry.setTargetId(discardingPlayerId);
            entry.setNonTargeting(true);
        }
        entry.setTriggeringCards(discardedCards);
        for (Card card : discardedCards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        if (discardedCards.size() == 1) {
            Card card = discardedCards.getFirst();
            entry.setTriggeringCard(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        return entry;
    }
}
'''
assert src.rstrip().endswith("}")
src = src.rstrip()[:-1].rstrip() + "\n" + default_event
src = src.replace("import lombok.RequiredArgsConstructor;",
                  "import com.github.laxika.magicalvibes.model.GameData;\nimport lombok.RequiredArgsConstructor;", 1)
PATH.write_text(src)

# The two callers pass the event.
TCS = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/TriggerCollectionService.java"
t = TCS.read_text()
for old, new in [("discardTriggerCollectorService.collectCycling(match, effect)",
                  "discardTriggerCollectorService.collectCycling(match, effect, ctx)"),
                 ("discardTriggerCollectorService.collectOpponentCausedDiscard(match, effect)",
                  "discardTriggerCollectorService.collectOpponentCausedDiscard(match, effect, ctx)")]:
    if t.count(old) != 1:
        sys.exit("caller " + old)
    t = t.replace(old, new)
TCS.write_text(t)
print("converted the discard collector")
