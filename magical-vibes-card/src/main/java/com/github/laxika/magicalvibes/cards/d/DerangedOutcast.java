package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "112")
public class DerangedOutcast extends Card {

    public DerangedOutcast() {
        // {1}{G}, Sacrifice a Human: Put two +1/+1 counters on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)
                                )),
                                "Sacrifice a Human",
                                false
                        ),
                        new PutPlusOnePlusOneCounterOnTargetCreatureEffect(2)
                ),
                "{1}{G}, Sacrifice a Human: Put two +1/+1 counters on target creature."
        ));
    }
}
