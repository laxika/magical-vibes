package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BFZ", collectorNumber = "120")
public class PainfulTruths extends Card {

    public PainfulTruths() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER));
    }
}
