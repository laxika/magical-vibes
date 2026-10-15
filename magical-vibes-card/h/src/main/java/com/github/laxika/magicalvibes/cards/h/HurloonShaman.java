package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "WTH", collectorNumber = "108")
public class HurloonShaman extends Card {

    public HurloonShaman() {
        // When this creature dies, each player sacrifices a land of their choice.
        addEffect(EffectSlot.ON_DEATH, new SacrificePermanentsEffect(
                1, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
