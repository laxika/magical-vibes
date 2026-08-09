package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "NEM", collectorNumber = "128")
public class ComplexAutomaton extends Card {

    public ComplexAutomaton() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(7, new PermanentTruePredicate()), ReturnToHandEffect.self()));
    }
}
