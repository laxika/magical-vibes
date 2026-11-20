package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "54")
public class SinisterStrength extends Card {

    public SinisterStrength() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 1, Subjects.attachedCreature()))
                .addStaticEffect(new GrantColorEffect(CardColor.BLACK, Subjects.attachedCreature(), true));
    }
}
