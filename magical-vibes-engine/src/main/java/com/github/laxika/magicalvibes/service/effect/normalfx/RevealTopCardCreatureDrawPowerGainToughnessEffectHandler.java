package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureDrawPowerGainToughnessEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link RevealTopCardCreatureDrawPowerGainToughnessEffect}: reveals the top card of the
 * controller's library and, if it is a creature card, draws that many cards (its power) and gains
 * that much life (its toughness). The revealed card is never moved by the reveal itself, so the
 * first draw takes it off the top.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureDrawPowerGainToughnessEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureDrawPowerGainToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").").build());

        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        int power = topCard.getPower() != null ? Math.max(0, topCard.getPower()) : 0;
        int toughness = topCard.getToughness() != null ? Math.max(0, topCard.getToughness()) : 0;

        for (int i = 0; i < power; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        if (toughness > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, toughness, sourceName,
                    entry.getCard(), entry.getEntryType());
        }

        log.info("Game {} - {} reveals creature {} (P/T {}/{}) via {}",
                gameData.id, playerName, topCard.getName(), power, toughness, sourceName);
    }
}
