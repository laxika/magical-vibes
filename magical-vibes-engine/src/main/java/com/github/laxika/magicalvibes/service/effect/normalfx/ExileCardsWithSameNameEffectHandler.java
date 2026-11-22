package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.PendingInteraction.MultiZoneExileChoice.SearchedZones;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect.Find;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;

/**
 * Resolves {@link ExileCardsWithSameNameEffect}: what must be exiled leaves first, then the effect's controller is
 * asked about the rest, the step pausing at its end. The answer adds to the step and shuffles the searched library.
 */
@Component
@RequiredArgsConstructor
public class ExileCardsWithSameNameEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final LibrarySearchSupport librarySearchSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardsWithSameNameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardsWithSameNameEffect) effect;
        // Both are read before the step records anything: "that card" may be the exile step's before this one.
        Set<String> names = new LinkedHashSet<>();
        List<UUID> searched = new ArrayList<>();
        List<LocatedCard> namedInHand = new ArrayList<>();
        if (e.cards() != null) {
            for (LocatedCard named : subjectResolutionService.locatedCards(gameData, entry, e, e.cards())) {
                if (named.zone() == Zone.HAND) {
                    namedInHand.add(named);
                }
                names.addAll(named.card().getNames());
                if (e.searched() == null && !searched.contains(named.ownerId())) {
                    searched.add(named.ownerId());
                }
            }
        } else if (e.permanents() != null) {
            names.addAll(subjectResolutionService.names(gameData, entry, e, e.permanents()));
        } else if (entry.getChosenCardName() != null) {
            names.add(entry.getChosenCardName());
        }
        if (e.searched() != null) {
            searched.addAll(subjectResolutionService.players(gameData, entry, e, e.searched()));
        }
        UUID sourceId = e.withSource() ? entry.getSourcePermanentId() : null;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);

        if (e.find() == Find.EVERY) {
            exileEvery(gameData, entry, e, names, searched, sourceId);
            return;
        }
        // "Search its controller's", "search its owner's": one player's zones.
        if (!searched.isEmpty()) {
            if (e.find() == Find.ALL && e.zones().contains(Zone.HAND)) {
                exileNamedInHand(gameData, entry, namedInHand, searched.getFirst(), sourceId);
            }
            search(gameData, entry, e, names, searched.getFirst(), sourceId);
        }
    }

    private void exileEvery(GameData gameData, StackEntry entry, ExileCardsWithSameNameEffect e, Set<String> names,
                            List<UUID> players, UUID sourceId) {
        List<Permanent> permanents = new ArrayList<>();
        if (e.zones().contains(Zone.BATTLEFIELD)) {
            for (UUID playerId : players) {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                    if (names.stream().anyMatch(name -> gameQueryService.permanentHasName(gameData, permanent, name))) {
                        permanents.add(permanent);
                    }
                }
            }
        }
        for (UUID playerId : players) {
            if (e.zones().contains(Zone.HAND)) {
                playerInteractionSupport.resolveRevealHand(gameData, playerId);
                exileFromZone(gameData, entry, playerId, Zone.HAND, names, sourceId);
            }
            if (e.zones().contains(Zone.GRAVEYARD)) {
                exileFromZone(gameData, entry, playerId, Zone.GRAVEYARD, names, sourceId);
            }
        }
        exilePermanents(gameData, entry, permanents, sourceId);
    }

    private void search(GameData gameData, StackEntry entry, ExileCardsWithSameNameEffect e, Set<String> names,
                        UUID playerId, UUID sourceId) {
        UUID searcherId = entry.getControllerId();
        boolean shuffles = e.zones().contains(Zone.LIBRARY);
        List<Card> offered = new ArrayList<>();
        Set<Zone> searchedZones = new LinkedHashSet<>();
        int libraryTopCards = 0;
        // "All cards with the same name as that card", that card being in the graveyard searched, and "all cards with
        // that name", the chosen one: the graveyard's are found with the others as one exile, whatever is picked from
        // the hidden zones.
        boolean graveyardFoundWithTheRest = e.find() == Find.ALL && e.permanents() == null
                && e.zones().contains(Zone.GRAVEYARD);
        for (Zone zone : List.of(Zone.HAND, Zone.GRAVEYARD, Zone.LIBRARY)) {
            if (!e.zones().contains(zone)) {
                continue;
            }
            List<Card> cards = zone(gameData, playerId, zone);
            if (zone == Zone.LIBRARY) {
                // A library that can't be searched is neither looked at nor offered, though still shuffled; the rest
                // is done as far as possible (CR 609.3).
                if (librarySearchSupport.isSearchPrevented(gameData, searcherId, playerId, false, searcherId)) {
                    continue;
                }
                // "That player searches the top four cards of that library instead."
                libraryTopCards = librarySearchSupport.opponentSearchTopCardsLimit(gameData, searcherId);
                cards = cards.subList(0, Math.min(libraryTopCards, cards.size()));
            }
            searchedZones.add(zone);
            if (zone == Zone.GRAVEYARD && e.find() == Find.ALL && !graveyardFoundWithTheRest) {
                exileFromZone(gameData, entry, playerId, zone, names, sourceId);
                continue;
            }
            offered.addAll(matching(cards, names));
        }
        if (names.isEmpty() && e.cards() != null && searchedZones.contains(Zone.LIBRARY)
                && searchedZones.contains(Zone.HAND)) {
            // "Search that player's graveyard, hand, and library" with no card chosen: there is nothing to find, but
            // the library may still be searched (CR 701.23a).
            librarySearchSupport.sendLibrarySearchToPlayer(gameData, searcherId,
                    LibrarySearchParams.builder(searcherId, List.<Card>of())
                            .targetPlayerId(playerId)
                            .canFailToFind(true)
                            .shuffleAfterSelection(true)
                            .build(),
                    "You may search the library; no card was named, so there is nothing to find.", true,
                    gameData.playerIdToName.get(searcherId) + " may search "
                            + gameData.playerIdToName.get(playerId) + "'s library.");
            return;
        }
        if (!offered.isEmpty()) {
            playerInputService.beginSameNameExileChoice(gameData, searcherId, offered, e.maxCount(), playerId,
                    String.join(", ", names), sourceId, new SearchedZones(searchedZones, libraryTopCards), shuffles,
                    graveyardFoundWithTheRest);
            return;
        }
        if (shuffles && gameData.playerDecks.get(playerId) != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " shuffles their library."));
        }
    }

    /** "The chosen card plus all others": the named card itself, which is known to be there, needs no finding. */
    private void exileNamedInHand(GameData gameData, StackEntry entry, List<LocatedCard> named, UUID playerId,
                                  UUID sourceId) {
        for (LocatedCard located : named) {
            Card card = located.card();
            if (!playerId.equals(located.ownerId())
                    || !gameData.playerHands.get(playerId).removeIf(held -> held.getId().equals(card.getId()))) {
                continue;
            }
            if (sourceId == null) {
                exileService.exileCard(gameData, playerId, card);
            } else {
                exileService.exileCard(gameData, playerId, card, sourceId);
            }
            entry.addToStep(StepKind.EXILE,
                    StepOutcome.ofObjects(List.of(card.getId()), List.of(playerId)).withFromHand(1));
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " is exiled from " + gameData.playerIdToName.get(playerId) + "'s hand."));
        }
    }

    private void exileFromZone(GameData gameData, StackEntry entry, UUID playerId, Zone zone, Set<String> names,
                               UUID sourceId) {
        List<Card> cards = zone(gameData, playerId, zone);
        List<UUID> exiled = new ArrayList<>();
        List<Card> leaving = matching(cards, names);
        if (zone == Zone.GRAVEYARD) {
            permanentRemovalService.removeCardsFromGraveyardForExile(gameData, playerId, leaving);
        } else if (!leaving.isEmpty()) {
            cards.removeAll(leaving);
        }
        for (Card card : leaving) {
            if (sourceId == null) {
                exileService.exileCard(gameData, playerId, card);
            } else {
                exileService.exileCard(gameData, playerId, card, sourceId);
            }
            exiled.add(card.getId());
            gameLogService.append(gameData, GameLog.cardThen(card, " is exiled from "
                    + gameData.playerIdToName.get(playerId) + "'s "
                    + (zone == Zone.HAND ? "hand." : "graveyard.")));
        }
        if (!exiled.isEmpty()) {
            entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(exiled, List.of(playerId))
                    .withFromHand(zone == Zone.HAND ? exiled.size() : 0));
        }
    }

    private void exilePermanents(GameData gameData, StackEntry entry, List<Permanent> permanents, UUID sourceId) {
        if (permanents.isEmpty()) {
            return;
        }
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : permanents) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                List<Card> cards = List.copyOf(permanent.cardsLeavingBattlefield());
                if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                    continue;
                }
                List<MovedCard> moved = new ArrayList<>();
                for (Card card : cards) {
                    var exiled = card.isToken() ? null : gameData.findExiledCard(card.getId());
                    if (exiled != null) {
                        moved.add(new MovedCard(permanent.getId(), card.getId(), Zone.EXILE, exiled.ownerId()));
                    }
                }
                entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(permanent.getId()),
                        controllerId == null ? List.of() : List.of(controllerId), moved));
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private static List<Card> zone(GameData gameData, UUID playerId, Zone zone) {
        List<Card> cards = switch (zone) {
            case HAND -> gameData.playerHands.get(playerId);
            case GRAVEYARD -> gameData.playerGraveyards.get(playerId);
            case LIBRARY -> gameData.playerDecks.get(playerId);
            default -> null;
        };
        return cards == null ? List.of() : cards;
    }

    private static List<Card> matching(List<Card> cards, Set<String> names) {
        return cards.stream().filter(card -> names.stream().anyMatch(card::hasName)).toList();
    }
}
