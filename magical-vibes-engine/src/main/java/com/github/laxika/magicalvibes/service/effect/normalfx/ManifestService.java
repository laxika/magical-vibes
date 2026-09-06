package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
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
    private final GameLogService gameLogService;

    public Permanent manifestTopCard(GameData gameData, UUID playerId, Card sourceCard) {
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " cannot manifest because its controller's library is empty."));
            return null;
        }

        Card manifestedCard = library.removeFirst();
        return manifestCardAndReturnPermanent(gameData, playerId, sourceCard, manifestedCard);
    }

    public boolean manifestCard(GameData gameData, UUID playerId, Card sourceCard, Card manifestedCard) {
        return manifestCardAndReturnPermanent(gameData, playerId, sourceCard, manifestedCard) != null;
    }

    private Permanent manifestCardAndReturnPermanent(GameData gameData, UUID playerId,
                                                     Card sourceCard, Card manifestedCard) {
        Permanent manifested = putManifestedCard(gameData, playerId, manifestedCard, new ArrayList<>(),
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));

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
