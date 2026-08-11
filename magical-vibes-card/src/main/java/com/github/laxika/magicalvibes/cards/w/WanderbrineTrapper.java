package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "42")
public class WanderbrineTrapper extends Card {

    public WanderbrineTrapper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, false),
                        new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{1}, {T}, Tap another untapped creature you control: Tap target creature an opponent controls.",
                TargetFilters.creatureAnOpponentControls()
        ));
    }
}
