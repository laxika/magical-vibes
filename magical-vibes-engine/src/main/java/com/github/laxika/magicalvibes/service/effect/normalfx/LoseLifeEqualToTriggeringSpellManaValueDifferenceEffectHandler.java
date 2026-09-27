package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves life loss based on the triggering spell's snapshotted mana-value difference. */
@Component
@RequiredArgsConstructor
public class LoseLifeEqualToTriggeringSpellManaValueDifferenceEffectHandler
        implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEqualToTriggeringSpellManaValueDifferenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        int amount = Math.max(0, entry.getEventValue() - entry.getXValue());
        if (amount > 0) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId, amount, entry.getCard().getName());
        }
    }
}
