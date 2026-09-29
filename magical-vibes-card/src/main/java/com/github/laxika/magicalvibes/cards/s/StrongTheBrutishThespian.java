package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GiveControllerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RadiationLifeGainReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "PIP", collectorNumber = "84")
@CardRegistration(set = "PIP", collectorNumber = "403")
@CardRegistration(set = "PIP", collectorNumber = "612")
@CardRegistration(set = "PIP", collectorNumber = "931")
public class StrongTheBrutishThespian extends Card {

    public StrongTheBrutishThespian() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE, SequenceEffect.of(
                new GiveControllerRadCountersEffect(3),
                new PutCountersOnSourceEffect(1, 1, 3)));
        addEffect(EffectSlot.STATIC, new RadiationLifeGainReplacementEffect());
    }
}
