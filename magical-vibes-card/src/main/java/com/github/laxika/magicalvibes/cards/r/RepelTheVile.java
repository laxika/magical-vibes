package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "33")
public class RepelTheVile extends Card {

    public RepelTheVile() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target creature with power 4 or greater",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(4))),
                                "Target must be a creature with power 4 or greater.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile target enchantment",
                        new ExileTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsEnchantmentPredicate(),
                                "Target must be an enchantment."))
        )));
    }
}
