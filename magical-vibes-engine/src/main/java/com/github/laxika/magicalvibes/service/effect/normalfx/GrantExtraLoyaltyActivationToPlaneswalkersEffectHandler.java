package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToPlaneswalkersEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GrantExtraLoyaltyActivationToPlaneswalkersEffect}: every planeswalker the
 * controller controls gets one extra loyalty activation for the rest of the turn. The grant is recorded
 * per player, so planeswalkers that come under the controller's control later in the turn get it too.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GrantExtraLoyaltyActivationToPlaneswalkersEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantExtraLoyaltyActivationToPlaneswalkersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        gameData.playerExtraLoyaltyActivationsThisTurn.merge(entry.getControllerId(), 1, Integer::sum);
        log.info("Game {} - planeswalkers of {} may activate an extra loyalty ability this turn",
                gameData.id, entry.getControllerId());
    }
}
