#!/usr/bin/env python3
"""Step 15, run 17z12d: the cards-put-into-a-graveyard collectors record the cards the event was about, with the
graveyard stays they went to, whatever the effects are (`MiscTriggerCollectorService.recordCardsPutIntoGraveyard`);
Hedge Shredder's and Colossal Grave-Reaver's class-keyed handlers, which judged "one or more land / creature cards" as
the ability triggered, are gone: those are the cards' trigger filters now."""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SVC = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service"


def edit(path, pairs):
    src = path.read_text()
    for old, new in pairs:
        if src.count(old) != 1:
            sys.exit(f"{path.name}: expected one match of\n{old}")
        src = src.replace(old, new)
    path.write_text(src)


def delete_method(src, name):
    sig = src.find("    private boolean " + name + "(")
    if sig < 0:
        sys.exit("no " + name)
    # Back up over the annotations directly above the method.
    begin = sig
    while True:
        prev = src.rfind("\n", 0, begin - 1)
        line = src[prev + 1:begin - 1].strip()
        if line.startswith("@") or line.startswith("watch =") or line.endswith(","):
            begin = prev + 1
            continue
        break
    end = src.index("\n    }\n", sig) + len("\n    }\n")
    if src[end:end + 1] == "\n":
        end += 1
    return src[:begin] + src[end:]


misc = SVC / "trigger/MiscTriggerCollectorService.java"
src = misc.read_text()
src = delete_method(src, "handleMilledLandsReturn")
src = delete_method(src, "handleEventCardsGraveyardReturn")
misc.write_text(src)
edit(misc, [
    ("""            if (triggeringCard != null) {
                // "That card" is the graveyard object the card became (CR 400.7).
                entry.setTriggeringCard(triggeringCard.getId(), gameData.graveyardEntryVersion(triggeringCard.getId()));
            }
            if (ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut) {
                entry.setTriggeringCards(cardsPut.cards());
            }
            gameData.enqueueTrigger(entry);""",
     """            if (ctx instanceof TriggerContext.CardsPutIntoGraveyardFromLibrary cardsPut) {
                recordCardsPutIntoGraveyard(gameData, entry, cardsPut.cards());
            } else if (triggeringCard != null) {
                recordCardsPutIntoGraveyard(gameData, entry, List.of(triggeringCard));
            }
            gameData.enqueueTrigger(entry);"""),
    ("""        if (ctx instanceof TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary batch) {
            entry.setEventValue(batch.creatureCardCount());
        }
        gameData.enqueueTrigger(entry);""",
     """        if (ctx instanceof TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary batch) {
            entry.setEventValue(batch.creatureCardCount());
            recordCardsPutIntoGraveyard(gameData, entry, batch.cards());
        } else if (ctx instanceof TriggerContext.CreatureCardPutIntoGraveyardFromLibrary cardPut) {
            recordCardsPutIntoGraveyard(gameData, entry, List.of(cardPut.creatureCard()));
        }
        gameData.enqueueTrigger(entry);"""),
])
src = misc.read_text().rstrip()
assert src.endswith("}")
src = src[:-1].rstrip() + '''

    /**
     * Records on {@code entry} the cards a put-into-a-graveyard event was about, whatever its effects are: as its
     * triggering cards ("them", "one of them"), each with the graveyard stay it went to (a card that left the graveyard
     * and returned is a new object, CR 400.7), and a lone card also as its triggering card ("that card").
     */
    static void recordCardsPutIntoGraveyard(GameData gameData, StackEntry entry, List<Card> cards) {
        entry.setTriggeringCards(cards);
        for (Card card : cards) {
            entry.getEventCardGraveyardEntryVersions().put(card.getId(), gameData.graveyardEntryVersion(card.getId()));
        }
        if (cards.size() == 1) {
            entry.setTriggeringCard(cards.getFirst().getId(), gameData.graveyardEntryVersion(cards.getFirst().getId()));
        }
    }
}
'''
misc.write_text(src)

ctxf = SVC / "trigger/TriggerContext.java"
edit(ctxf, [
    ("""    /** Context for ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY triggers (Sidisi, Brood Tyrant). */
    record CreatureCardsPutIntoGraveyardFromLibrary(UUID graveyardOwnerId, int creatureCardCount)
            implements TriggerContext {}""",
     """    /**
     * Context for ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY triggers (Sidisi, Brood Tyrant; Colossal
     * Grave-Reaver's "one of them"): the creature cards the event put there.
     */
    record CreatureCardsPutIntoGraveyardFromLibrary(UUID graveyardOwnerId, int creatureCardCount, List<Card> cards)
            implements TriggerContext {
        public CreatureCardsPutIntoGraveyardFromLibrary {
            cards = cards == null ? List.of() : List.copyOf(cards);
        }
    }"""),
])
tcs = SVC / "trigger/TriggerCollectionService.java"
edit(tcs, [
    ("new TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary(controllerId, events.size()));",
     "new TriggerContext.CreatureCardsPutIntoGraveyardFromLibrary(controllerId, events.size(),\n"
     "                            events.stream().map(ZoneChangeEvent::card).toList()));"),
])
zcw = SVC / "trigger/ZoneChangeWatch.java"
edit(zcw, [
    ("""    /** "Whenever one or more cards are put into your graveyard from your library". */
    ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE)),""",
     """    /**
     * "Whenever one or more cards are put into your graveyard from your library"; "one or more land cards" (Hedge
     * Shredder), whose event is the lands alone.
     */
    ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY(battlefield(
            Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN, ObjectRelation.YOU_OWN),
            TriggerMultiplicity.ONE_OR_MORE),
            battlefield(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.LAND_CARD, ObjectRelation.YOU_OWN),
                    TriggerMultiplicity.ONE_OR_MORE)),"""),
])

land = SVC / "effect/normalfx/ReturnTriggeringLandFromGraveyardToBattlefieldEffectHandler.java"
edit(land, [
    ("""        if (!entry.getEventCardIds().isEmpty()) {
            for (UUID cardId : entry.getEventCardIds()) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                Long version = entry.getEventCardGraveyardEntryVersions().get(cardId);
                if (card != null && (version == null || version == gameData.graveyardEntryVersion(cardId))) {
                    lands.add(card);
                }
            }
        } else if (entry.getTriggeringCardId() != null) {""",
     """        if (!entry.getTriggeringCards().isEmpty()) {
            for (UUID cardId : entry.triggeringCardIds()) {
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                Long version = entry.getEventCardGraveyardEntryVersions().get(cardId);
                if (card != null && (version == null ? !cardId.equals(entry.getTriggeringCardId())
                        || entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(cardId))
                        : version == gameData.graveyardEntryVersion(cardId))) {
                    lands.add(card);
                }
            }
        } else if (entry.getTriggeringCardId() != null) {"""),
    ("""/**
 * Returns the lands the trigger event was about (the entry's event cards, or else its triggering card) from the""",
     """/**
 * Returns the lands the trigger event was about (the entry's triggering cards, or else its triggering card) from the"""),
])
eff = ROOT / "magical-vibes-domain/src/main/java/com/github/laxika/magicalvibes/model/effect/ReturnTriggeringLandFromGraveyardToBattlefieldEffect.java"
edit(eff, [
    (""" * <p>The lands are the stack entry's event cards (with their graveyard entry versions) when it has any, otherwise its
 * triggering card""",
     """ * <p>The lands are the stack entry's triggering cards (with their graveyard entry versions) when it has any, otherwise its
 * triggering card"""),
])
grs = SVC / "effect/normalfx/GraveyardReturnSupport.java"
edit(grs, [
    ("""        Set<UUID> eventCardIds = effect.eventCardIdsOnly()
                ? new HashSet<>(entry.getEventCardIds())
                : Set.of();""",
     """        // "Those cards" are the cards the resolving spell acted on, else the cards the trigger event was about.
        Set<UUID> eventCardIds = !effect.eventCardIdsOnly() ? Set.of()
                : !entry.getEventCardIds().isEmpty() ? new HashSet<>(entry.getEventCardIds())
                : new HashSet<>(entry.triggeringCardIds());"""),
])
print("converted the milled-card collectors")
