package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "234")
public class AllFatesScroll extends Card {

    public AllFatesScroll() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(
                        new SacrificeSelfCost(),
                        new DrawCardEffect(new DistinctPermanentNamesCount(
                                new PermanentIsLandPredicate(), CountScope.CONTROLLER))
                ),
                "{7}, {T}, Sacrifice this artifact: Draw X cards, where X is the number of differently named lands you control."
        ));
    }
}
