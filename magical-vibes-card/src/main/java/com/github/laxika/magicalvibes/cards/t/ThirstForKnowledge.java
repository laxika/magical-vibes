package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MRD", collectorNumber = "53")
@CardRegistration(set = "MSH", collectorNumber = "79")
@CardRegistration(set = "DDF", collectorNumber = "68")
@CardRegistration(set = "HOP", collectorNumber = "14")
public class ThirstForKnowledge extends Card {

    public ThirstForKnowledge() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.CONTROLLER, CardType.ARTIFACT));
    }
}
