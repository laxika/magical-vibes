package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "100")
public class StunSniper extends Card {

    public StunSniper() {
        // {1}, {T}: deal 1 damage to target creature, then tap that same creature (both effects share the target).
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DealDamageToTargetCreatureEffect(1),
                        new TapPermanentsEffect(TapUntapScope.TARGET)
                ),
                "{1}, {T}: This creature deals 1 damage to target creature. Tap that creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
