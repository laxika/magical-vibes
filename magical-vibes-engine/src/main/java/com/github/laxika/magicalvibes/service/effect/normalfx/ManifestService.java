package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Shared implementation for manifesting the top card of a player's library. */
@Component
@RequiredArgsConstructor
public class ManifestService {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ObjectProvider<GraveyardReturnSupport> graveyardReturnSupport;

    public Permanent manifestTopCard(GameData gameData, UUID playerId, Card sourceCard) {
        return manifestTopCard(gameData, playerId, playerId, sourceCard);
    }

    /**
     * {@code controllerId} manifests the top card of {@code libraryOwnerId}'s library (CR 701.40a): it enters face
     * down as a 2/2 under the manifesting player's control, which holds as a control effect when the card came from
     * another player's library.
     */
    public Permanent manifestTopCard(GameData gameData, UUID libraryOwnerId, UUID controllerId, Card sourceCard) {
        List<Card> library = gameData.playerDecks.get(libraryOwnerId);
        boolean ownLibrary = libraryOwnerId.equals(controllerId);
        String libraryName = ownLibrary ? "its controller's library"
                : gameData.playerIdToName.get(libraryOwnerId) + "'s library";
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    ownLibrary ? " cannot manifest because its controller's library is empty."
                            : " cannot manifest because " + libraryName + " is empty."));
            return null;
        }

        if (gameQueryService.isManifestBlockedFromZone(gameData, library.getFirst(), Zone.LIBRARY)) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " cannot manifest the top card of " + libraryName + "."));
            return null;
        }

        Card manifestedCard = library.removeFirst();
        Permanent manifested = putManifestedCard(gameData, controllerId, manifestedCard, new ArrayList<>(),
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        if (!ownLibrary) {
            graveyardReturnSupport.getObject().trackStolenCreature(
                    gameData, manifested.getId(), controllerId, libraryOwnerId);
        }
        battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, manifestedCard);

        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " manifests the top card of " + libraryName + "."));
        return manifested;
    }

    public boolean manifestCard(GameData gameData, UUID playerId, Card sourceCard, Card manifestedCard) {
        return manifestCardAndReturnPermanent(gameData, playerId, sourceCard, manifestedCard) != null;
    }

    public void manifestCards(GameData gameData, UUID playerId, Card sourceCard, List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        var enterTappedTypesSnapshot = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (Card card : cards) {
            putManifestedCard(gameData, playerId, card, simultaneouslyEntered, enterTappedTypesSnapshot);
        }
        for (Card card : cards) {
            battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, playerId, card);
        }
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " manifests the cards."));
    }

    public Permanent manifestCardAndReturnPermanent(GameData gameData, UUID playerId,
                                                    Card sourceCard, Card manifestedCard) {
        Permanent manifested = putManifestedCard(gameData, playerId, manifestedCard, new ArrayList<>(),
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, playerId, manifestedCard);

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " manifests the top card of its controller's library."));
        return manifested;
    }

    public void manifestExiledCards(GameData gameData, UUID playerId, Card sourceCard, List<Card> cards) {
        List<Card> cardsToManifest = cards.stream()
                .filter(card -> gameData.removeFromExile(card.getId()))
                .toList();
        if (cardsToManifest.isEmpty()) {
            return;
        }

        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        var enterTappedTypesSnapshot = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (Card card : cardsToManifest) {
            putManifestedCard(gameData, playerId, card, simultaneouslyEntered, enterTappedTypesSnapshot);
        }
        for (Card card : cardsToManifest) {
            battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, playerId, card);
        }

        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " manifests the exiled cards."));
    }

    private Permanent putManifestedCard(GameData gameData, UUID playerId, Card card,
                                        List<Permanent> simultaneouslyEntered, Set<CardType> enterTappedTypesSnapshot) {
        Permanent manifested = new Permanent(card);
        manifested.setManifested(true);
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, playerId, manifested,
                enterTappedTypesSnapshot, simultaneouslyEntered);
        simultaneouslyEntered.add(manifested);
        return manifested;
    }
}
