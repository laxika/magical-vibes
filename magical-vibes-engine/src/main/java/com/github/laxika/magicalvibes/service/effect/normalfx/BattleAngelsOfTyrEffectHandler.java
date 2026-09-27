package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BattleAngelsOfTyrEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Battle Angels of Tyr's conditional combat-damage riders. */
@Component
@RequiredArgsConstructor
public class BattleAngelsOfTyrEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DrawCardEffectHandler drawCardEffectHandler;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GainLifeEffectHandler gainLifeEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BattleAngelsOfTyrEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null || !gameData.playerIds.contains(damagedPlayerId)) {
            return;
        }

        if (hasMoreCardsInHandThanEachOtherPlayer(gameData, damagedPlayerId)) {
            drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(1));
        }

        if (hasMoreLandsThanEachOtherPlayer(gameData, damagedPlayerId)) {
            createTokenEffectHandler.resolve(gameData, entry, CreateTokenEffect.ofTreasureToken(1));
        }

        if (hasMoreLifeThanEachOtherPlayer(gameData, damagedPlayerId)) {
            gainLifeEffectHandler.resolve(gameData, entry, new GainLifeEffect(3));
        }
    }

    private boolean hasMoreCardsInHandThanEachOtherPlayer(GameData gameData, UUID playerId) {
        int handSize = gameData.playerHands.getOrDefault(playerId, List.of()).size();
        return gameData.orderedPlayerIds.stream()
                .filter(otherPlayerId -> !otherPlayerId.equals(playerId))
                .allMatch(otherPlayerId -> handSize
                        > gameData.playerHands.getOrDefault(otherPlayerId, List.of()).size());
    }

    private boolean hasMoreLandsThanEachOtherPlayer(GameData gameData, UUID playerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(otherPlayerId -> !otherPlayerId.equals(playerId))
                .allMatch(otherPlayerId -> gameQueryService.controlsMoreLandsThan(
                        gameData, playerId, otherPlayerId));
    }

    private boolean hasMoreLifeThanEachOtherPlayer(GameData gameData, UUID playerId) {
        int life = gameData.getLife(playerId);
        return gameData.orderedPlayerIds.stream()
                .filter(otherPlayerId -> !otherPlayerId.equals(playerId))
                .allMatch(otherPlayerId -> life > gameData.getLife(otherPlayerId));
    }
}
