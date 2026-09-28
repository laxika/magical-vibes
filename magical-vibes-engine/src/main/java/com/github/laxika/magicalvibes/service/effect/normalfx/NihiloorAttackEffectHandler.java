package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.NihiloorAttackEffect;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Expands Nihiloor's attack trigger into the standard life-gain and life-loss effects. */
@Component
public class NihiloorAttackEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NihiloorAttackEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackerId = entry.getTargetId();
        if (attackerId == null) {
            return;
        }
        UUID ownerId = entry.getTriggeringPermanentOwnerId();
        if (ownerId == null) {
            ownerId = gameData.defaultControllerOf(attackerId);
        }
        if (ownerId == null || !gameData.playerIds.contains(ownerId)) {
            return;
        }

        entry.setTargetId(ownerId);
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                new GainLifeEffect(2),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)));
    }
}
