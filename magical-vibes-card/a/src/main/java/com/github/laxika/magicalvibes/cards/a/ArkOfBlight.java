package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "140")
public class ArkOfBlight extends Card {

    public ArkOfBlight() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{3}, {T}, Sacrifice this artifact: Destroy target land.",
                TargetFilters.land()
        ));
    }
}
