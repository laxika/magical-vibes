package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "3")
public class AetherstormRoc extends Card {

    private static final PermanentAllOfPredicate DEFENDING_PLAYER_CREATURE =
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentControlledByDefendingPlayerPredicate()));

    public AetherstormRoc() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnergyCountersEffect(1));

        target(new PermanentPredicateTargetFilter(
                DEFENDING_PLAYER_CREATURE,
                "Target must be a creature defending player controls"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                        ConditionalEffect.unless(new ControllerEnergyAtLeast(2),
                                SequenceEffect.of(
                                        new EnergyCountersEffect(-2),
                                        new PutCountersOnSourceEffect(1, 1, 1),
                                        new TapPermanentsEffect(TapUntapScope.TARGET, DEFENDING_PLAYER_CREATURE))),
                        "Pay {E}{E} to put a +1/+1 counter on Aetherstorm Roc and tap up to one target creature?"));
    }
}
