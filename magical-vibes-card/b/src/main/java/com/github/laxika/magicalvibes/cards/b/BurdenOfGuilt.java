package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "4")
public class BurdenOfGuilt extends Card {

    public BurdenOfGuilt() {
        target(TargetFilters.creature());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new TapPermanentsEffect(TapUntapScope.ENCHANTED)),
                "{1}: Tap enchanted creature."
        ));
    }
}
