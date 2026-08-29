package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "178")
public class JasperaSentinel extends Card {

    public JasperaSentinel() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, false),
                        new AwardAnyColorManaEffect()),
                "{T}, Tap an untapped creature you control: Add one mana of any color."
        ));
    }
}
