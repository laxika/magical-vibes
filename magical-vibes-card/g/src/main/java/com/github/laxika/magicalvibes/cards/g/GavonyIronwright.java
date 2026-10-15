package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DKA", collectorNumber = "9")
public class GavonyIronwright extends Card {

    public GavonyIronwright() {
        // Fateful hour — As long as you have 5 or less life, other creatures you control get +1/+4.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerLifeAtMost(5), new StaticBoostEffect(1, 4, GrantScope.OWN_CREATURES)));
    }
}
