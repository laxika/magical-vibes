package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "ONE", collectorNumber = "65")
public class PrologueToPhyresis extends Card {

    public PrologueToPhyresis() {
        addEffect(EffectSlot.SPELL, new GivePoisonCountersEffect(1, PoisonRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
