package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "205")
public class MineLayer extends Card {

    private static final PermanentPredicate MINED_LAND = new PermanentAllOfPredicate(List.of(
            new PermanentIsLandPredicate(),
            new PermanentHasCountersPredicate(CounterType.MINE)));

    public MineLayer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new PutCountersEffect(CounterType.MINE)),
                "{1}{R}, {T}: Put a mine counter on target land.",
                TargetFilters.land()));

        addTrigger(List.of(
                        Triggers.permanentYouControlBecomesTapped().withFilter(MINED_LAND),
                        Triggers.permanentOpponentControlsBecomesTapped().withFilter(MINED_LAND)),
                new DestroyTargetPermanentEffect(Subjects.triggering()));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                RemoveCountersEffect.allFrom(Subjects.allLands(), CounterType.MINE));
    }
}
