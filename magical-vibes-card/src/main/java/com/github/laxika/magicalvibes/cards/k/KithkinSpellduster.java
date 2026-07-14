package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

// Flying and Persist are loaded from Scryfall and resolved automatically
// (PermanentRemovalService.collectPersistTrigger + PersistReturnEffect).
@CardRegistration(set = "EVE", collectorNumber = "8")
public class KithkinSpellduster extends Card {

    public KithkinSpellduster() {
        // {1}{W}, Sacrifice this creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{1}{W}, Sacrifice Kithkin Spellduster: Destroy target enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsEnchantmentPredicate(),
                        "Target must be an enchantment"
                )
        ));
    }
}
