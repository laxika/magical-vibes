package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "VMA", collectorNumber = "250")
@CardRegistration(set = "2XM", collectorNumber = "194")
@CardRegistration(set = "NCC", collectorNumber = "336")
@CardRegistration(set = "DSC", collectorNumber = "86")
@CardRegistration(set = "M3C", collectorNumber = "259")
@CardRegistration(set = "C18", collectorNumber = "174")
public class DeathreapRitual extends Card {

    public DeathreapRitual() {
        // Morbid — At the beginning of each end step, if a creature died this turn,
        // you may draw a card.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new Morbid(),
                new MayEffect(new DrawCardEffect(), "Draw a card?")));
    }
}
