package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "6")
public class Caregiver extends Card {

    public Caregiver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new SacrificeCreatureCost(), PreventDamageEffect.nextToTarget(1)),
                "{W}, Sacrifice a creature: Prevent the next 1 damage that would be dealt to any target this turn."
        ));
    }
}
