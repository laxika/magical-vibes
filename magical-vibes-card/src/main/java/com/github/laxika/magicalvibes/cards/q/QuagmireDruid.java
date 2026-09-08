package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "51")
public class QuagmireDruid extends Card {

    public QuagmireDruid() {
        // {G}, {T}, Sacrifice a creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new SacrificeCreatureCost(), new DestroyTargetPermanentEffect()),
                "{G}, {T}, Sacrifice a creature: Destroy target enchantment.",
                TargetFilters.enchantment()
        ));
    }
}
