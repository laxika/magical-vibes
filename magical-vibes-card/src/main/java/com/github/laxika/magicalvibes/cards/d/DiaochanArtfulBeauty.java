package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCreatureToDestroyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PTK", collectorNumber = "108")
public class DiaochanArtfulBeauty extends Card {

    public DiaochanArtfulBeauty() {
        // {T}: Destroy target creature of your choice (the ability's target), then an opponent chooses
        // any creature to destroy (resolved via OpponentChoosesCreatureToDestroyEffect).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect(false), new OpponentChoosesCreatureToDestroyEffect()),
                "{T}: Destroy target creature of your choice, then destroy target creature of an opponent's choice. "
                        + "Activate only during your turn, before attackers are declared.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"),
                null,
                null,
                ActivationTimingRestriction.ONLY_BEFORE_ATTACKERS_DECLARED
        ));
    }
}
