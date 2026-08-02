package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "6")
public class Auratog extends Card {

    public Auratog() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsEnchantmentPredicate(),
                                "Sacrifice an enchantment",
                                false),
                        new BoostSelfEffect(2, 2)
                ),
                "Sacrifice an enchantment: This creature gets +2/+2 until end of turn."
        ));
    }
}
