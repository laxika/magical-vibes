package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "216")
public class TectonicBreak extends Card {

    public TectonicBreak() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                new XValue(), new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
