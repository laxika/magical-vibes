package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilBasicLandToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilBasicLandToHandRestToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilBasicLandToHandRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        List<Card> revealed = new ArrayList<>();
        Card basicLand = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC)) {
                basicLand = card;
                break;
            }
        }

        if (revealed.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed with " + cardName + "."));
            return;
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + "."));

        List<Card> rest = new ArrayList<>(revealed);
        if (basicLand != null) {
            rest.remove(basicLand);
            gameData.playerHands.get(controllerId).add(basicLand);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " puts " + basicLand.getName() + " into their hand."));
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    playerName + " reveals their entire library — no basic land found."));
        }

        for (Card card : rest) {
            graveyardService.addCardToGraveyard(gameData, controllerId, card);
        }

        log.info("Game {} - {} resolving {} — basicLand={}, {} cards to graveyard",
                gameData.id, playerName, cardName,
                basicLand != null ? basicLand.getName() : "none", rest.size());
    }
}
