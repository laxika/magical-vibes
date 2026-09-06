package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandIfDashCostPaidEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "102")
public class GoblinHeelcutter extends Card {

    public GoblinHeelcutter() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnSelfToHandIfDashCostPaidEffect());

        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target creature"
        )).addEffect(EffectSlot.ON_ATTACK, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
