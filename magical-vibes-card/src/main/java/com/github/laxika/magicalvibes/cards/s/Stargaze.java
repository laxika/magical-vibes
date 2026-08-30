package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "BLB", collectorNumber = "114")
public class Stargaze extends Card {

    public Stargaze() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsEffect(
                new Scaled(new XValue(), 2), new XValue(), null,
                LookDestination.GRAVEYARD, false));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER));
    }
}
