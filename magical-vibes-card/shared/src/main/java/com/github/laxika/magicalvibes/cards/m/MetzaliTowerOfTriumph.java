package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

public class MetzaliTowerOfTriumph extends Card {

    public MetzaliTowerOfTriumph() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new DealDamageToPlayersEffect(2, Subjects.eachOpponent())),
                "{1}{R}, {T}: Metzali deals 2 damage to each opponent."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(ChoosePermanentsEffect.atRandom(1, Subjects.controlledBy(Subjects.eachPlayer(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentAttackedThisTurnPredicate()))),
                                "a creature that attacked this turn"),
                        new DestroyTargetPermanentEffect(Subjects.thisWay(StepKind.CHOOSE))),
                "{2}{W}, {T}: Choose a creature at random that attacked this turn. Destroy that creature."
        ));
    }
}
