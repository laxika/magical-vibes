package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "242")
public class StaffOfCompleation extends Card {

    public StaffOfCompleation() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new DestroyTargetPermanentEffect()),
                "{T}, Pay 1 life: Destroy target permanent you own.",
                new OwnedPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(), "Target must be a permanent you own")
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(2), new AwardAnyColorManaEffect()),
                "{T}, Pay 2 life: Add one mana of any color."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(3), new ProliferateEffect()),
                "{T}, Pay 3 life: Proliferate."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(4), new DrawCardEffect(1)),
                "{T}, Pay 4 life: Draw a card."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{5}: Untap this artifact."
        ));
    }
}
