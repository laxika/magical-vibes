package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardPutIntoHandAndChangeLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPutIntoHandAndChangeLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        boolean gainLife = ((RevealTopCardPutIntoHandAndChangeLifeEffect) effect).gainLife();

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.removeFirst();
        int manaValue = topCard.getManaValue();

        // Reveal the card and put it into hand.
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + topCard.getName() + " (mana value " + manaValue + ") from the top of their library."));
        gameData.addCardToHand(controllerId, topCard);

        // Change life equal to the revealed card's mana value.
        if (manaValue > 0) {
            if (gainLife) {
                lifeSupport.applyGainLife(gameData, controllerId, manaValue, sourceName,
                        entry.getCard(), entry.getEntryType());
            } else {
                lifeSupport.applyLifeLoss(gameData, controllerId, manaValue, sourceName);
            }
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + topCard.getName() + " into their hand (" + sourceName + ")."));
        }

        log.info("Game {} - {} reveals {} (MV {}) via {}", gameData.id, playerName, topCard.getName(), manaValue, sourceName);
    }
}
