package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "26")
public class EtherswornAdjudicator extends Card {

    public EtherswornAdjudicator() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}{B}",
                List.of(new DestroyTargetPermanentEffect()),
                "{1}{W}{B}, {T}: Destroy target creature or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be a creature or enchantment."
                )
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{2}{U}: Untap this creature."
        ));
    }
}
