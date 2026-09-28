package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PhabineParleyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhabineParleyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final BoostAllOwnCreaturesEffectHandler boostAllOwnCreaturesEffectHandler;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhabineParleyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        int landCount = 0;
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
            if (topCard.hasType(CardType.LAND)) {
                landCount++;
            } else {
                nonlandCount++;
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " reveals ", topCard, " from the top of their library (" + sourceName + ")."));
        }

        if (landCount > 0) {
            createTokenEffectHandler.resolveForController(gameData, entry,
                    new CreateTokenEffect(landCount, "Citizen", 1, 1, CardColor.GREEN,
                            Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN)),
                    entry.getControllerId());
            if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
                return;
            }
        }

        if (nonlandCount > 0) {
            boostAllOwnCreaturesEffectHandler.resolve(gameData, entry,
                    new BoostAllOwnCreaturesEffect(nonlandCount, nonlandCount));
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
        }

        log.info("Game {} - {} resolves Phabine's Parley for {} lands and {} nonlands",
                gameData.id, sourceName, landCount, nonlandCount);
    }
}
