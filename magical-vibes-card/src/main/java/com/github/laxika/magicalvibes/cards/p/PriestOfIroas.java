package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "134")
public class PriestOfIroas extends Card {

    public PriestOfIroas() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{3}{W}, Sacrifice Priest of Iroas: Destroy target enchantment.",
                TargetFilters.enchantment()
        ));
    }
}
