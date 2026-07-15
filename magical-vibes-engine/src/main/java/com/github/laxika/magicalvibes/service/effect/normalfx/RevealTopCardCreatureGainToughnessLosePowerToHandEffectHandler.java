package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureGainToughnessLosePowerToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureGainToughnessLosePowerToHandEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureGainToughnessLosePowerToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // Reveal the top card. It only leaves the library if it's a creature card.
        Card topCard = deck.getFirst();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + topCard.getName() + " from the top of their library (" + sourceName + ")."));

        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        deck.removeFirst();

        int toughness = topCard.getToughness() != null ? topCard.getToughness() : 0;
        int power = topCard.getPower() != null ? topCard.getPower() : 0;

        // Gain life equal to toughness, then lose life equal to power, then put it into hand.
        if (toughness > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, toughness, sourceName,
                    entry.getCard(), entry.getEntryType());
        }
        if (power > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId, power, sourceName);
        }

        gameData.addCardToHand(controllerId, topCard);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + topCard.getName() + " into their hand (" + sourceName + ")."));

        log.info("Game {} - {} reveals creature {} (P/T {}/{}) via {}",
                gameData.id, playerName, topCard.getName(), power, toughness, sourceName);
    }
}
