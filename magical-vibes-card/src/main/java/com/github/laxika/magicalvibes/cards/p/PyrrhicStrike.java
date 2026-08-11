package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "30")
public class PyrrhicStrike extends Card {

    public PyrrhicStrike() {
        addEffect(EffectSlot.SPELL,
                new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 2, true));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(
                List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Destroy target artifact or enchantment",
                                new DestroyTargetPermanentEffect(),
                                new PermanentPredicateTargetFilter(
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentIsEnchantmentPredicate())),
                                        "Target must be an artifact or enchantment")),
                        new ChooseOneEffect.ChooseOneOption(
                                "Destroy target creature with mana value 3 or greater",
                                new DestroyTargetPermanentEffect(),
                                new PermanentPredicateTargetFilter(
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentMinManaValuePredicate(3))),
                                        "Target must be a creature with mana value 3 or greater"))),
                false, 1, 2, true));
    }
}
