package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.OpponentsWhoLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilThenEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "220")
public class KaitoBaneOfNightmares extends Card {

    public KaitoBaneOfNightmares() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllConditions(List.of(
                        new ControllerTurn(),
                        new SourceCounterThreshold(1, CounterType.LOYALTY)
                )),
                new AnimatePermanentsEffect(3, 4, List.of(CardSubtype.NINJA), Set.of(Keyword.HEXPROOF))));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateEmblemEffect(
                        List.of(new StaticBoostEffect(
                                1, 1, GrantScope.OWN_CREATURES,
                                new PermanentHasSubtypePredicate(CardSubtype.NINJA)
                        )),
                        "Ninjas you control get +1/+1."
                )),
                "+1: You get an emblem with \"Ninjas you control get +1/+1.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                0,
                List.of(SurveilThenEffect.direct(
                        2,
                        new DrawCardEffect(new OpponentsWhoLostLifeThisTurn())
                )),
                "0: Surveil 2. Then draw a card for each opponent who lost life this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new PutCounterOnTargetPermanentEffect(CounterType.STUN, 2)
                ),
                "−2: Tap target creature. Put two stun counters on it.",
                TargetFilters.creature()
        ));
    }
}
