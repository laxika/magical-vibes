package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SHM", collectorNumber = "226")
public class ElvishHexhunter extends Card {

    public ElvishHexhunter() {
        // {G/W}, {T}, Sacrifice this creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G/W}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{G/W}, {T}, Sacrifice Elvish Hexhunter: Destroy target enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsEnchantmentPredicate(),
                        "Target must be an enchantment"
                )
        ));
    }
}
