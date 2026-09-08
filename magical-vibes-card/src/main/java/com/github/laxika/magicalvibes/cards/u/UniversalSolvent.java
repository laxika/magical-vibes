package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "178")
public class UniversalSolvent extends Card {

    public UniversalSolvent() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{7}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{7}, {T}, Sacrifice this artifact: Destroy target permanent.",
                TargetFilters.permanent()
        ));
    }
}
