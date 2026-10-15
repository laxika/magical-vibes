package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "14")
public class FaithHealer extends Card {

    public FaithHealer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsEnchantmentPredicate(),
                                "Sacrifice an enchantment",
                                false,
                                false,
                                true,
                                false),
                        new GainLifeEffect(new XValue())
                ),
                "Sacrifice an enchantment: You gain life equal to the sacrificed enchantment's mana value."
        ));
    }
}
