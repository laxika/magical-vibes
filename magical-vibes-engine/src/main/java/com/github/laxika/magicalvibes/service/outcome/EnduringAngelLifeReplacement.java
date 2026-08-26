package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.EnduringAngelLifeTotalReplacementEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnimationSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Applies Enduring Angel's life-total replacement before ordinary loss replacements. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EnduringAngelLifeReplacement implements LossReplacer {

    private final GameQueryService gameQueryService;
    private final AnimationSupport animationSupport;
    private final LifeSupport lifeSupport;

    public EnduringAngelLifeReplacement(GameQueryService gameQueryService,
                                        AnimationSupport animationSupport,
                                        LifeSupport lifeSupport) {
        this.gameQueryService = gameQueryService;
        this.animationSupport = animationSupport;
        this.lifeSupport = lifeSupport;
    }

    @Override
    public boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason) {
        if (reason != LossReason.LIFE || losingPlayerId == null) {
            return false;
        }

        if (!gameQueryService.canPlayerLifeChange(gameData, losingPlayerId)) {
            return false;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(losingPlayerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent angel : battlefield) {
            boolean hasReplacement = angel.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(EnduringAngelLifeTotalReplacementEffect.class::isInstance);
            if (hasReplacement && animationSupport.transformToBackFace(gameData, angel)) {
                return lifeSupport.applySetLifeTotal(gameData, losingPlayerId, 3);
            }
        }
        return false;
    }
}
