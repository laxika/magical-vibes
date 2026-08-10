package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "167")
public class ElfReplica extends Card {

    public ElfReplica() {
        // {1}{G}, Sacrifice this creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{1}{G}, Sacrifice Elf Replica: Destroy target enchantment.",
                TargetFilters.enchantment()
        ));
    }
}
