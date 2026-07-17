package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "169")
public class InitiatesOfTheEbonHand extends Card {

    public InitiatesOfTheEbonHand() {
        // {1}: Add {B}. If this ability has been activated four or more times this turn,
        // sacrifice this creature at the beginning of the next end step.
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new AwardManaEffect(ManaColor.BLACK)),
                "{1}: Add {B}. If this ability has been activated four or more times this turn, sacrifice this creature at the beginning of the next end step."));

        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(new ActivationCount(4, 0), new SacrificeSelfEffect()));
    }
}
