package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostOpponentCreaturesByPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "ONC", collectorNumber = "12")
@CardRegistration(set = "ONC", collectorNumber = "50")
public class PhyresisOutbreak extends Card {

    public PhyresisOutbreak() {
        addEffect(EffectSlot.SPELL, new GivePoisonCountersEffect(1, PoisonRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new BoostOpponentCreaturesByPoisonCountersEffect());
    }
}
