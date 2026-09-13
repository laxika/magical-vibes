package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ParleyEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParleyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AwardManaEffectHandler awardManaEffectHandler;
    private final LifeSupport lifeSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ParleyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        int nonlandCount = 0;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + "'s library is empty; no card is revealed (" + sourceName + ")."));
                continue;
            }

            Card topCard = deck.getFirst();
            if (!topCard.hasType(CardType.LAND)) {
                nonlandCount++;
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " reveals ", topCard, " from the top of their library (" + sourceName + ")."));
        }

        if (nonlandCount > 0) {
            awardManaEffectHandler.resolve(gameData, entry,
                    new AwardManaEffect(ManaColor.GREEN, nonlandCount));
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), nonlandCount, sourceName,
                    entry.getCard(), entry.getEntryType());
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        }

        log.info("Game {} - {} resolves Parley for {} nonland cards", gameData.id, sourceName, nonlandCount);
    }
}
