package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttackDirectionRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.RestrictAttacksToDirectionUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Teyo's temporary global attack-direction restriction. */
@Component
public class RestrictAttacksToDirectionUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RestrictAttacksToDirectionUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId != null) {
            gameData.expireFloatingEffects(floating -> sourcePermanentId.equals(floating.sourcePermanentId())
                    && floating.effect() instanceof AttackDirectionRestrictionEffect);
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard() == null ? "Attack Direction Restriction" : entry.getCard().getName(),
                entry.getSourcePermanentId(),
                entry.getControllerId(),
                effect,
                null,
                null,
                new PermanentTruePredicate(),
                Duration.UNTIL_YOUR_NEXT_TURN,
                0));
    }
}
