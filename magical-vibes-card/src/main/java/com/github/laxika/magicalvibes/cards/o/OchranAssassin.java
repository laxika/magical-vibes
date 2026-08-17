package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "GRN", collectorNumber = "194")
public class OchranAssassin extends Card {

    public OchranAssassin() {
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());
    }
}
