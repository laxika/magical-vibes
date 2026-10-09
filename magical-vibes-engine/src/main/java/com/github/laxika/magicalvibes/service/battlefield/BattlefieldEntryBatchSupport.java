package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.BattlefieldEntryLibraryRemainder;
import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/** Chooses legal attachments before putting a batch of cards onto the battlefield together. */
@Component
@RequiredArgsConstructor
public class BattlefieldEntryBatchSupport {
    private final AuraAttachmentService auraAttachmentService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    @Autowired
    @Lazy
    private CloneService cloneService;
    @Autowired
    @Lazy
    private BattlefieldPlacementService battlefieldPlacementService;

    public void begin(GameData gameData, List<BattlefieldEntryCard> cards) {
        begin(gameData, cards, List.of());
    }

    public void begin(GameData gameData, List<BattlefieldEntryCard> cards,
                      List<BattlefieldEntryLibraryRemainder> libraryRemainders) {
        continueChoices(gameData, cards, new ArrayList<>(), libraryRemainders);
    }

    public void completeChoice(GameData gameData, UUID attachmentId,
                               PermanentChoiceContext.AuraEntryBatchChoice choice) {
        List<BattlefieldEntryCard> ready = new ArrayList<>(choice.ready());
        ready.add(choice.choosingProtector()
                ? choice.remaining().getFirst().withProtector(attachmentId)
                : choice.remaining().getFirst().withAttachment(attachmentId));
        continueChoices(gameData, choice.remaining().subList(1, choice.remaining().size()), ready,
                choice.libraryRemainders());
    }

    public void completeClonePreparation(GameData gameData, Permanent permanent,
                                         List<StackEntry> triggers,
                                         PermanentChoiceContext.AuraEntryBatchChoice choice) {
        List<BattlefieldEntryCard> remaining = new ArrayList<>(choice.remaining());
        remaining.set(0, remaining.getFirst().withPreparedPermanent(permanent, triggers));
        continueChoices(gameData, remaining, new ArrayList<>(choice.ready()), choice.libraryRemainders());
    }

    private void continueChoices(GameData gameData, List<BattlefieldEntryCard> remaining,
                                 List<BattlefieldEntryCard> ready,
                                 List<BattlefieldEntryLibraryRemainder> libraryRemainders) {
        for (int i = 0; i < remaining.size(); i++) {
            BattlefieldEntryCard candidate = remaining.get(i);
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, candidate.card(), candidate.origin())) {
                continue;
            }
            if (candidate.preparedPermanent() == null && cloneService.prepareCloneReplacementEffect(
                    gameData, candidate.controllerId(), candidate.card(), null)) {
                gameData.cloneOperation.preparedPermanent = new Permanent(candidate.card(), candidate.origin());
                gameData.cloneOperation.battlefieldEntryBatch = new PermanentChoiceContext.AuraEntryBatchChoice(
                        remaining.subList(i, remaining.size()), ready, false, libraryRemainders);
                return;
            }
            Card enteringCard = candidate.enteringCard();
            if (enteringCard.getSubtypes().contains(CardSubtype.SIEGE)
                    && candidate.protectorPlayerId() == null) {
                List<UUID> opponents = gameData.orderedPlayerIds.stream()
                        .filter(id -> !id.equals(candidate.controllerId()))
                        .toList();
                if (opponents.size() > 1) {
                    playerInputService.beginPermanentChoice(gameData, candidate.controllerId(), opponents,
                            new PermanentChoiceContext.AuraEntryBatchChoice(remaining.subList(i, remaining.size()), ready, true, libraryRemainders),
                            "Choose an opponent to protect " + enteringCard.getName() + ".");
                    return;
                }
                ready.add(opponents.isEmpty() ? candidate : candidate.withProtector(opponents.getFirst()));
                continue;
            }
            if (!enteringCard.isAura() || enteringCard.isEnchantZone()) {
                ready.add(candidate);
                continue;
            }
            List<UUID> hosts = new ArrayList<>();
            if (!enteringCard.isEnchantPlayer()) {
                gameData.forEachPermanent((controllerId, permanent) -> {
                    if (!gameQueryService.cantBeEnchantedByOtherAuras(gameData, permanent)
                            && auraAttachmentService.canEnchant(gameData, enteringCard, candidate.controllerId(), permanent)) {
                        hosts.add(permanent.getId());
                    }
                });
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (auraAttachmentService.canEnchantPlayer(gameData, enteringCard, candidate.controllerId(), playerId)) {
                    hosts.add(playerId);
                }
            }
            if (hosts.isEmpty()) continue;
            if (hosts.size() == 1) {
                ready.add(candidate.withAttachment(hosts.getFirst()));
            } else {
                playerInputService.beginPermanentChoice(gameData, candidate.controllerId(), hosts,
                        new PermanentChoiceContext.AuraEntryBatchChoice(remaining.subList(i, remaining.size()), ready, false, libraryRemainders),
                        "Choose what " + enteringCard.getName() + " will enchant.");
                return;
            }
        }
        prepareNativeChoices(gameData, ready, new ArrayList<>(), libraryRemainders);
    }

    /** Resumes the same batch after a native replacement choice without placing that card alone. */
    public boolean completeNativeChoice(GameData gameData, BattlefieldEntryRequest request) {
        var batch = gameData.pendingBattlefieldEntryBatch;
        UUID cardId = request.permanent().getOriginalCard().getId();
        if (batch == null || !gameData.pendingBattlefieldEntryRequests.containsKey(cardId)) return false;
        List<BattlefieldEntryCard> remaining = new ArrayList<>(batch.remaining());
        if (remaining.isEmpty() || !remaining.getFirst().card().getId().equals(cardId)) return false;
        remaining.set(0, remaining.getFirst().withPreparedPermanent(
                request.permanent(), remaining.getFirst().afterEntryTriggers()));
        gameData.pendingBattlefieldEntryRequests.put(cardId, request);
        prepareNativeChoices(gameData, remaining, new ArrayList<>(batch.ready()), batch.libraryRemainders());
        return true;
    }

    public boolean completeNativeChoice(GameData gameData, Permanent permanent) {
        var request = gameData.pendingBattlefieldEntryRequests.get(permanent.getOriginalCard().getId());
        return request != null && completeNativeChoice(gameData, request.withPermanent(permanent, List.of()));
    }

    /** Removes a declined entry from its original zone before its replacement destination is applied. */
    public void removeRejectedCardFromOrigin(GameData gameData, Permanent permanent) {
        var batch = gameData.pendingBattlefieldEntryBatch;
        if (batch != null && !batch.remaining().isEmpty()
                && batch.remaining().getFirst().card().getId().equals(permanent.getOriginalCard().getId())) {
            removeFromOrigin(gameData, batch.remaining().getFirst());
        }
    }

    public void completeRejectedEntry(GameData gameData, Permanent permanent) {
        var batch = gameData.pendingBattlefieldEntryBatch;
        if (batch == null || batch.remaining().isEmpty()
                || !batch.remaining().getFirst().card().getId().equals(permanent.getOriginalCard().getId())) return;
        gameData.pendingBattlefieldEntryRequests.remove(permanent.getOriginalCard().getId());
        prepareNativeChoices(gameData, batch.remaining().subList(1, batch.remaining().size()),
                new ArrayList<>(batch.ready()), batch.libraryRemainders());
    }

    private void prepareNativeChoices(GameData gameData, List<BattlefieldEntryCard> remaining,
                                      List<BattlefieldEntryCard> ready,
                                      List<BattlefieldEntryLibraryRemainder> remainders) {
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (int i = 0; i < remaining.size(); i++) {
            BattlefieldEntryCard candidate = remaining.get(i);
            Permanent permanent = candidate.preparedPermanent() == null
                    ? new Permanent(candidate.card(), candidate.origin()) : candidate.preparedPermanent();
            permanent.setAttachedTo(candidate.attachmentId());
            permanent.setProtectorPlayerId(candidate.protectorPlayerId());
            if (candidate.origin() == Zone.EXILE) permanent.setEnteredFromExile(true);
            BattlefieldEntryCard prepared = candidate.withPreparedPermanent(permanent, candidate.afterEntryTriggers());
            BattlefieldEntryRequest request = gameData.pendingBattlefieldEntryRequests.get(candidate.card().getId());
            request = request == null ? new BattlefieldEntryRequest(candidate.controllerId(), permanent,
                    enterTappedTypes, List.of(), 0, false, List.of()) : request.withPermanent(permanent, List.of());
            gameData.pendingBattlefieldEntryRequests.put(candidate.card().getId(), request);
            List<BattlefieldEntryCard> pending = new ArrayList<>(remaining.subList(i, remaining.size()));
            pending.set(0, prepared);
            gameData.pendingBattlefieldEntryBatch = new PermanentChoiceContext.AuraEntryBatchChoice(
                    pending, ready, false, remainders);
            if (battlefieldEntryService.beginNativeCardNameChoice(gameData, request)) return;
            if (battlefieldPlacementService.prepareNativeBatchChoices(gameData, request)) return;
            if (request.unleashChoice() == null) request = request.withUnleashChoice(false);
            if (request.riotCounters() == null) request = request.withRiotChoices(0, false);
            gameData.pendingBattlefieldEntryRequests.put(candidate.card().getId(), request);
            ready.add(prepared);
        }
        gameData.pendingBattlefieldEntryBatch = null;
        placeCards(gameData, ready);
        gameData.pendingBattlefieldEntryRequests.clear();
        finishLibraryRemainders(gameData, remainders);
    }

    private void finishLibraryRemainders(GameData gameData, List<BattlefieldEntryLibraryRemainder> remainders) {
        for (BattlefieldEntryLibraryRemainder remainder : remainders) {
            List<Card> deck = gameData.playerDecks.get(remainder.playerId());
            if (deck == null) continue;
            List<Card> cards = new ArrayList<>(remainder.cards());
            var revealedIds = cards.stream().map(Card::getId).collect(java.util.stream.Collectors.toSet());
            deck.removeIf(card -> revealedIds.contains(card.getId()));
            if (!remainder.shuffleLibrary()) Collections.shuffle(cards);
            deck.addAll(cards);
            if (remainder.shuffleLibrary()) LibraryShuffleHelper.shuffleLibrary(gameData, remainder.playerId());
        }
    }

    private void placeCards(GameData gameData, List<BattlefieldEntryCard> cards) {
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entered = new ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (BattlefieldEntryCard candidate : cards) {
                if (!removeFromOrigin(gameData, candidate)) continue;
                Permanent permanent = candidate.preparedPermanent() == null
                        ? new Permanent(candidate.card(), candidate.origin()) : candidate.preparedPermanent();
                permanent.setAttachedTo(candidate.attachmentId());
                if (candidate.tapped()) permanent.tap();
                if (candidate.origin() == Zone.EXILE) permanent.setEnteredFromExile(true);
                permanent.setProtectorPlayerId(candidate.protectorPlayerId());
                if (candidate.origin() == Zone.GRAVEYARD) {
                    permanent.setEnteredFromGraveyardOwnerId(candidate.zoneOwnerId());
                }
                BattlefieldEntryRequest request = gameData.pendingBattlefieldEntryRequests.get(candidate.card().getId());
                if (request == null) {
                    battlefieldEntryService.putPermanentOntoBattlefield(gameData, candidate.controllerId(), permanent,
                            enterTappedTypes, List.copyOf(entered));
                } else {
                    battlefieldPlacementService.place(gameData,
                            request.withBatchState(permanent, enterTappedTypes, List.copyOf(entered)));
                }
                if (gameQueryService.findPermanentController(gameData, permanent.getId()) != null) {
                    entered.add(permanent);
                    gameData.stack.addAll(candidate.afterEntryTriggers());
                    gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " enters the battlefield."));
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (Permanent permanent : entered) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId != null) {
                battlefieldEntryService.processCreatureETBEffects(gameData, controllerId, permanent.getCard(), null, false);
            }
        }
    }

    private boolean removeFromOrigin(GameData gameData, BattlefieldEntryCard candidate) {
        List<Card> zone = switch (candidate.origin()) {
            case GRAVEYARD -> gameData.playerGraveyards.get(candidate.zoneOwnerId());
            case LIBRARY -> gameData.playerDecks.get(candidate.zoneOwnerId());
            default -> gameData.playerHands.get(candidate.zoneOwnerId());
        };
        boolean removed = candidate.origin() == Zone.EXILE
                ? gameData.removeFromExile(candidate.card().getId())
                : zone != null && zone.removeIf(card -> card.getId().equals(candidate.card().getId()));
        if (removed && candidate.origin() == Zone.GRAVEYARD) {
            graveyardService.notifyCardsLeftGraveyard(gameData, candidate.zoneOwnerId(), candidate.card());
        }
        return removed;
    }
}
