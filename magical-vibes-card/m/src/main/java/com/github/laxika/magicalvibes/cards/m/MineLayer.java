package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromMatchingPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "205")
public class MineLayer extends Card {

    private static final TriggeringPermanentConditionalEffect DESTROY_MINED_LAND =
            new TriggeringPermanentConditionalEffect(
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsLandPredicate(),
                            new PermanentHasCountersPredicate(CounterType.MINE))),
                    new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));

    public MineLayer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.MINE)),
                "{1}{R}, {T}: Put a mine counter on target land.",
                TargetFilters.land()));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, DESTROY_MINED_LAND);
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, DESTROY_MINED_LAND);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new RemoveAllCountersFromMatchingPermanentsEffect(
                        CounterType.MINE, new PermanentIsLandPredicate()));
    }
}
