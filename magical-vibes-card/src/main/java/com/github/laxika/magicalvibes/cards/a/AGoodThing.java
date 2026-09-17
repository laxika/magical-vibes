package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OwnEffectsCantAffectSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MB1", collectorNumber = "86")
public class AGoodThing extends Card {

    public AGoodThing() {
        addEffect(EffectSlot.STATIC, new OwnEffectsCantAffectSourceEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new DoubleControllerLifeEffect(),
                ConditionalEffect.unless(new ControllerLifeAtLeast(1_000), new ControllerLosesGameEffect())));
    }
}
