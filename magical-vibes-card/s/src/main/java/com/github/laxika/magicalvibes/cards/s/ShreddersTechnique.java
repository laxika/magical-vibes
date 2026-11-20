package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "77")
@CardRegistration(set = "TMT", collectorNumber = "232")
public class ShreddersTechnique extends Card {

    public ShreddersTechnique() {
        addSneak("{B}");
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                "Target must be a creature or enchantment"))
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new ConditionalEffect(
                        new AllOf(List.of(HappenedThisWay.destroyed(),
                                new TargetPermanentMatches(new PermanentIsEnchantmentPredicate()))),
                        new LoseLifeEffect(2)));
    }
}
