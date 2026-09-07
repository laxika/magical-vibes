package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "274")
public class VoyagerStaff extends Card {

    public VoyagerStaff() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), FlickerEffect.exileTargetReturnAtEndStep()),
                "{2}, Sacrifice this artifact: Exile target creature. Return the exiled card to the battlefield under its owner's control at the beginning of the next end step.",
                TargetFilters.creature()
        ));
    }
}
