package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameFromLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "DST", collectorNumber = "128")
public class LichsTomb extends Card {

    public LichsTomb() {
        addEffect(EffectSlot.STATIC, new CantLoseGameFromLifeEffect());
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_LIFE, new SacrificePermanentsEffect(
                new EventValue(), new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER));
    }
}
