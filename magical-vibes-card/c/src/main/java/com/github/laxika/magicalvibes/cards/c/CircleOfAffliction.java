package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "PLC", collectorNumber = "66")
public class CircleOfAffliction extends Card {

    public CircleOfAffliction() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseColorOnEnterEffect());
        addEffect(EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE,
                new MayPayManaEffect("{1}",
                        SequenceEffect.of(
                                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER),
                                new GainLifeEffect(1)),
                        "Pay {1} to drain target player?"));
    }
}
