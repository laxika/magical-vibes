package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamageToPlaneswalkersYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MOC", collectorNumber = "70")
public class TheWesternCloud extends Card {

    public TheWesternCloud() {
        addStaticEffect(new PreventAllDamageToCreaturesYouControlEffect(null));
        addStaticEffect(new PreventFixedDamageToPlaneswalkersYouControlEffect(Integer.MAX_VALUE));

        addEffect(EffectSlot.CHAOS_TRIGGERED, SequenceEffect.of(
                CreateTokenEffect.ofTappedTreasureToken(3),
                new MassDamageEffect(3, false, false, true, null)));
    }
}
