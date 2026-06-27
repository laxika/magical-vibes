package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachSubtypeOnBattlefieldEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GainLifeForEachSubtypeOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeForEachSubtypeOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainLifeForEachSubtypeOnBattlefieldEffect) effect;
        int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().getSubtypes().contains(e.subtype())) {
                count[0]++;
            }
        });
        if (count[0] == 0) {
            return;
        }
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), count[0]);
    }
}
