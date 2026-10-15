package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "295")
public class Thaumatog extends Card {

    public Thaumatog() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new BoostSelfEffect(1, 1)
                ),
                "Sacrifice a land: This creature gets +1/+1 until end of turn."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsEnchantmentPredicate(), "Sacrifice an enchantment", false),
                        new BoostSelfEffect(1, 1)
                ),
                "Sacrifice an enchantment: This creature gets +1/+1 until end of turn."
        ));
    }
}
