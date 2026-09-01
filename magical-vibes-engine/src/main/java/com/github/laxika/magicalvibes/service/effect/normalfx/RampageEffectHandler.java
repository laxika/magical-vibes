package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RampageEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RampageEffectHandler implements NormalEffectHandlerBean {

    private final BoostSelfEffectHandler boostSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RampageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RampageEffect rampage = (RampageEffect) effect;
        var blockersBeyondTheFirst = new Max(
                new Sum(new CreaturesBlockingSource(), new Fixed(-1)), new Fixed(0));
        boostSelfEffectHandler.resolve(gameData, entry, new BoostSelfEffect(
                new Scaled(blockersBeyondTheFirst, rampage.bonusPerAdditionalBlocker()),
                new Scaled(blockersBeyondTheFirst, rampage.bonusPerAdditionalBlocker())));
    }
}
