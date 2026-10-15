package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1295")
public class MirrorOfGaladriel extends Card {

    public MirrorOfGaladriel() {
        PermanentCount legendaryCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
                )),
                CountScope.CONTROLLER
        );

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(
                        new ReduceActivationCostEffect(legendaryCreatures),
                        new ScryEffect(1),
                        new DrawCardEffect(1)
                ),
                "{5}, {T}: Scry 1, then draw a card. This ability costs {1} less to activate for each legendary creature you control."
        ));
    }
}
