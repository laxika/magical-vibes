package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCreatureCastCostInHandEffect;

@CardRegistration(set = "YBLB", collectorNumber = "17")
public class FountainportCharmer extends Card {

    public FountainportCharmer() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(),
                        new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyReduceCreatureCastCostInHandEffect());
    }
}
