package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ParleyCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParleyCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ParleyCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ParleyCreateTokensEffect parley = (ParleyCreateTokensEffect) effect;
        String sourceName = entry.getCard().getName();
        int nonlandCount = 0;

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (library == null || library.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + "'s library is empty; no card is revealed (" + sourceName + ")."));
                continue;
            }

            Card topCard = library.getFirst();
            if (!topCard.hasType(CardType.LAND)) {
                nonlandCount++;
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " reveals ", topCard, " from the top of their library (" + sourceName + ")."));
        }

        if (nonlandCount > 0) {
            createTokenEffectHandler.resolveForController(gameData, entry,
                    parley.token().withAmount(nonlandCount), entry.getControllerId());
            if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
                return;
            }
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        }

        log.info("Game {} - {} resolves token Parley for {} nonland cards",
                gameData.id, sourceName, nonlandCount);
    }
}
