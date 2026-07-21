package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "15")
public class CatharCommando extends Card {

    public CatharCommando() {
        // {1}, Sacrifice this creature: Destroy target artifact or enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{1}, Sacrifice Cathar Commando: Destroy target artifact or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                )
        ));
    }
}
