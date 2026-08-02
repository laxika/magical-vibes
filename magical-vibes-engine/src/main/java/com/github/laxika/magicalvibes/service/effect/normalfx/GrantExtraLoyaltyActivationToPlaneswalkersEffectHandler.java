package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToPlaneswalkersEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves {@link GrantExtraLoyaltyActivationToPlaneswalkersEffect}: every planeswalker the
 * controller controls gets one extra loyalty activation for the rest of the turn. Planeswalkers that
 * enter after resolution are not affected — the grant is applied to the permanents present as the
 * ability resolves.
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
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }
        for (Permanent permanent : battlefield) {
            if (!permanent.getCard().hasType(CardType.PLANESWALKER)) {
                continue;
            }
            permanent.setExtraLoyaltyActivationsThisTurn(permanent.getExtraLoyaltyActivationsThisTurn() + 1);
            log.info("Game {} - {} may activate an extra loyalty ability this turn",
                    gameData.id, permanent.getCard().getName());
        }
    }
}
