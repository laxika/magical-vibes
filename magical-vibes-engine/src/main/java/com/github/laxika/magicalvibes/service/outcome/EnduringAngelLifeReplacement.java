package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Applies Enduring Angel's life-total replacement before ordinary loss replacements, for a life total that is
 * already 0 or less when state-based actions are checked. Reductions made by life loss, life payment and setting
 * the life total are replaced as they happen by {@link LifeSupport}.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EnduringAngelLifeReplacement implements LossReplacer {

    private final LifeSupport lifeSupport;

    public EnduringAngelLifeReplacement(LifeSupport lifeSupport) {
        this.lifeSupport = lifeSupport;
    }

    @Override
    public boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason) {
        if (reason != LossReason.LIFE || losingPlayerId == null) {
            return false;
        }
        return lifeSupport.applyEnduringAngelReplacement(gameData, losingPlayerId);
    }
}
