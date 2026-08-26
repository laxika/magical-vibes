package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromTargetToAnotherTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "223")
public class RaziaBorosArchangel extends Card {

    public RaziaBorosArchangel() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RedirectNextDamageFromTargetToAnotherTargetEffect(3)),
                "{T}: The next 3 damage that would be dealt to target creature you control this turn is dealt to another target creature instead.",
                List.of(TargetFilters.creatureYouControl(), TargetFilters.creature()),
                2,
                2
        ));
    }
}
