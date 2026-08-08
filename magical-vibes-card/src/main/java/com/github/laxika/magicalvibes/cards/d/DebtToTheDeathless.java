package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "DGM", collectorNumber = "64")
public class DebtToTheDeathless extends Card {

    public DebtToTheDeathless() {
        addEffect(EffectSlot.SPELL,
                new LoseLifeEffect(new Scaled(new XValue(), 2), LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
