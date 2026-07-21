package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBaseStatsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link GrantBaseStatsToCounterBearersEffect} by installing the source-independent
 * floating continuous effects that make "every creature with a {@code counterType} counter has
 * base P/T {@code power}/{@code toughness} and has {@code keywords}" a game-wide rule.
 *
 * <p>The base-P/T set (layer 7b) and keyword grant (layer 6) are recorded as
 * {@link FloatingContinuousEffect}s with a {@code null} source and a {@code PERMANENT} duration,
 * scoped by {@link PermanentHasCountersPredicate}: the layered pass applies them to exactly the
 * creatures that currently hold such a counter, so the effect survives the creating card leaving
 * the battlefield and lapses as soon as the counter is removed. Establishing the same rule twice
 * (a second copy of the source) is a no-op.
 */
@Slf4j
@Component
public class GrantBaseStatsToCounterBearersEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantBaseStatsToCounterBearersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var rule = (GrantBaseStatsToCounterBearersEffect) effect;

        if (ruleAlreadyEstablished(gameData, rule)) {
            return;
        }

        String sourceName = entry.getCard() != null ? entry.getCard().getName() : null;
        UUID controllerId = entry.getControllerId();
        var scope = new PermanentHasCountersPredicate(rule.counterType());

        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                controllerId, new SetBasePowerToughnessEffect(rule.power(), rule.toughness(), GrantScope.ALL_CREATURES),
                null, null, scope, EffectDuration.PERMANENT, 0));

        if (!rule.keywords().isEmpty()) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                    controllerId, new GrantKeywordEffect(rule.keywords(), GrantScope.ALL_CREATURES),
                    null, null, scope, EffectDuration.PERMANENT, 0));
        }

        log.info("Game {} - established rule: creatures with a {} counter have base {}/{} and {}",
                gameData.id, rule.counterType(), rule.power(), rule.toughness(), rule.keywords());
    }

    private boolean ruleAlreadyEstablished(GameData gameData, GrantBaseStatsToCounterBearersEffect rule) {
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect fe : gameData.floatingEffects) {
                if (fe.scope() instanceof PermanentHasCountersPredicate p
                        && p.counterType() == rule.counterType()
                        && fe.effect() instanceof SetBasePowerToughnessEffect s
                        && s.power() == rule.power() && s.toughness() == rule.toughness()) {
                    return true;
                }
            }
        }
        return false;
    }
}
