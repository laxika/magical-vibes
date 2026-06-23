package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "112")
public class DerangedOutcast extends Card {

    public DerangedOutcast() {
        // {1}{G}, Sacrifice a Human: Put two +1/+1 counters on target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(
                        new SacrificeSubtypeCreatureCost(CardSubtype.HUMAN),
                        new PutPlusOnePlusOneCounterOnTargetCreatureEffect(2)
                ),
                "{1}{G}, Sacrifice a Human: Put two +1/+1 counters on target creature."
        ));
    }
}
