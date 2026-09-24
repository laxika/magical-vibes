package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "731")
@CardRegistration(set = "CMM", collectorNumber = "762")
public class DemonOfFatesDesign extends Card {

    public DemonOfFatesDesign() {
        addEffect(EffectSlot.STATIC,
                AlternativeCostForSpellsEffect.payLifeEqualToManaValueOnceDuringControllerTurn(
                        new CardTypePredicate(CardType.ENCHANTMENT)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsEnchantmentPredicate(), "another enchantment", true,
                                false, true, false),
                        new BoostSelfEffect(new XValue(), new Fixed(0))
                ),
                "{2}{B}, Sacrifice another enchantment: Demon of Fate's Design gets +X/+0 until end of turn, where X is the sacrificed enchantment's mana value."
        ));
    }
}
