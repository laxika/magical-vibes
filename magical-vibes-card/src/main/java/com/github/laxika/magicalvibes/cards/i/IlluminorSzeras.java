package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "37")
public class IlluminorSzeras extends Card {

    public IlluminorSzeras() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(
                                new PermanentIsCreaturePredicate(), "another creature", true, false, true, false),
                        new AwardManaEffect(ManaColor.BLACK, new XValue())),
                "{T}, Sacrifice another creature: Add an amount of {B} equal to the sacrificed creature's mana value."
        ));
    }
}
