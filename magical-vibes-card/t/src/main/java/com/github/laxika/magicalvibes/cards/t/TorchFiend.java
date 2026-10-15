package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "106")
@CardRegistration(set = "M13", collectorNumber = "151")
@CardRegistration(set = "M15", collectorNumber = "166")
public class TorchFiend extends Card {

    public TorchFiend() {
        // {R}, Sacrifice this creature: Destroy target artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{R}, Sacrifice Torch Fiend: Destroy target artifact.",
                TargetFilters.artifact()
        ));
    }
}
