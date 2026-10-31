package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "ONE", collectorNumber = "97")
public class InfectiousInquiry extends Card {

    public InfectiousInquiry() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LoseLifeEffect(2));
        addSpellEffect(new GivePoisonCountersEffect(1, PoisonRecipient.EACH_OPPONENT));
    }
}
