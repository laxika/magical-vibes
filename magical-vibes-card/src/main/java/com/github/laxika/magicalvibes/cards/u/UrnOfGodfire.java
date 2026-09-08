package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "266")
public class UrnOfGodfire extends Card {

    public UrnOfGodfire() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}: Add one mana of any color."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{6}, {T}, Sacrifice this artifact: Destroy target creature or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be a creature or enchantment"
                )
        ));
    }
}
