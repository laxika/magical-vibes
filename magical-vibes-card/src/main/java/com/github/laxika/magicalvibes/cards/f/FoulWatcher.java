package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "MH2", collectorNumber = "43")
public class FoulWatcher extends Card {

    public FoulWatcher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new Delirium(), new StaticBoostEffect(1, 0, GrantScope.SELF)));
    }
}
