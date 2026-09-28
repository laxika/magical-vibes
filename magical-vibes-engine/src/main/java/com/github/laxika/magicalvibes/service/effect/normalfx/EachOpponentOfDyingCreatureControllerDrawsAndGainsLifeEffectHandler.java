package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Bounty Board's reward against the dying creature controller's opponents. */
@Component
public class EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;

    public EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffectHandler(
            PlayerInteractionSupport playerInteractionSupport, LifeSupport lifeSupport) {
        this.playerInteractionSupport = playerInteractionSupport;
        this.lifeSupport = lifeSupport;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reward = (EachOpponentOfDyingCreatureControllerDrawsAndGainsLifeEffect) effect;
        UUID dyingCreatureControllerId = entry.getTargetId();
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
