package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ColorsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ECL", collectorNumber = "188")
public class Prismabasher extends Card {

    public Prismabasher() {
        targetUpTo(new ColorsAmongControlledPermanents(), new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Targets must be creatures you control"
        ), 5)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new BoostTargetCreatureEffect(
                                new ColorsAmongControlledPermanents(),
                                new ColorsAmongControlledPermanents()));
    }
}
