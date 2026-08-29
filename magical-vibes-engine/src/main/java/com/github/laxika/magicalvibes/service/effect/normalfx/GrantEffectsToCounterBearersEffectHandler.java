package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Installs source-independent continuous effects keyed to a counter on the affected permanent. */
@Component
public class GrantEffectsToCounterBearersEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectsToCounterBearersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var rule = (GrantEffectsToCounterBearersEffect) effect;
        var scope = new PermanentHasCountersPredicate(rule.counterType());

        if (ruleAlreadyEstablished(gameData, rule, scope)) {
            return;
        }

        String sourceName = entry.getCard() != null ? entry.getCard().getName() : null;
        UUID controllerId = entry.getControllerId();
        for (CardEffect grantedEffect : rule.effects()) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                    controllerId, grantedEffect, null, null, scope, EffectDuration.PERMANENT, 0));
        }
    }

    private boolean ruleAlreadyEstablished(GameData gameData, GrantEffectsToCounterBearersEffect rule,
                                           PermanentHasCountersPredicate scope) {
        synchronized (gameData.floatingEffects) {
            return rule.effects().stream().allMatch(grantedEffect -> gameData.floatingEffects.stream()
                    .anyMatch(floating -> scope.equals(floating.scope())
                            && grantedEffect.equals(floating.effect())));
        }
    }
}
