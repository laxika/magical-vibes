package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeToReduceColoredCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "119")
public class DefilerOfInstinct extends Card {

    private static CardAllOfPredicate redPermanentSpell() {
        return new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.RED),
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))
        ));
    }

    public DefilerOfInstinct() {
        CardAllOfPredicate redPermanentFilter = redPermanentSpell();
        addStaticEffect(new PayLifeToReduceColoredCastCostEffect(
                redPermanentFilter, "{R}", 2, CostModificationScope.SELF));
        addTrigger(Triggers.youCastSpell().withFilter(redPermanentFilter), new DealDamageToAnyTargetEffect(1));
    }
}
