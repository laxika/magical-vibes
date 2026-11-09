package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Bounty Board's reward against the opponents of the dying creature's controller: the stack entry's triggering
 * permanent's last-known controller (CR 608.2h).
 */
@Component
public class EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;

    public EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffectHandler(
            PlayerInteractionSupport playerInteractionSupport, LifeSupport lifeSupport,
            GameQueryService gameQueryService) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.lifeSupport = lifeSupport;
        this.gameQueryService = gameQueryService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reward = (EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect) effect;
        UUID dyingCreatureControllerId = gameQueryService.findTriggeringPermanentController(gameData, entry);
        if (dyingCreatureControllerId == null) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(dyingCreatureControllerId)) {
                continue;
            }
            playerInteractionSupport.applyDrawCards(gameData, playerId, 1);
            lifeSupport.applyGainLife(gameData, playerId, reward.lifeGain(), null,
                    entry.getCard(), entry.getEntryType());
        }
    }
}
