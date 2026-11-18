package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PutCounterOnCombatOpponentEffect}: puts the configured counters on the referenced
 * combat opponent (carried as the stack entry's non-targeting target) right away, provided it is
 * still on the battlefield (it need not still be a creature). Mindbender Spores.
 */
@Component
@RequiredArgsConstructor
public class PutCounterOnCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnCombatOpponentEffect counterEffect = (PutCounterOnCombatOpponentEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null || counterEffect.amount() <= 0) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, target, counterEffect.counterType(), counterEffect.amount());
        if (counterEffect.grantedStaticEffect() != null
                && gameData.floatingEffectsOnPermanent(target.getId(), GrantEffectEffect.class).stream()
                .noneMatch(fe -> ((GrantEffectEffect) fe.effect()).effect().equals(counterEffect.grantedStaticEffect()))) {
            gameData.grantAbilityToPermanent(entry, StaticAbility.of(counterEffect.grantedStaticEffect()),
                    target.getId(), Duration.PERMANENT);
        }
        if (counterEffect.grantedUpkeepEffect() != null) {
            gameData.grantAbilityToPermanent(entry, new TriggeredAbility(
                    Triggers.upkeep(ActivePlayer.YOU),
                    counterEffect.grantedUpkeepEffect()), target.getId(), Duration.PERMANENT);
        }
    }
}
