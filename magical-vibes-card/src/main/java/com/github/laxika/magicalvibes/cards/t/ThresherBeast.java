package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PCY", collectorNumber = "128")
public class ThresherBeast extends Card {

    public ThresherBeast() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED,
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), SacrificeRecipient.DEFENDING_PLAYER));
    }
}
