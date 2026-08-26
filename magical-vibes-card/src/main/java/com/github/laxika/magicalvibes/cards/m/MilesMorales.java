package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "108")
@CardRegistration(set = "SPM", collectorNumber = "200")
@CardRegistration(set = "SPM", collectorNumber = "211")
@CardRegistration(set = "SPM", collectorNumber = "234")
public class MilesMorales extends Card {

    public MilesMorales() {
        setBackFaceCard(new UltimateSpiderMan());
        setModalDoubleFaced(true);

        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}{G}{W}",
                List.of(new TransformSelfEffect()),
                "{3}{R}{G}{W}: Transform Miles Morales. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "UltimateSpiderMan";
    }
}

class UltimateSpiderMan extends Card {

    private static final PermanentPredicate SPIDER_OR_LEGENDARY_CREATURE = new PermanentAnyOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.SPIDER),
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)
            ))
    ));

    UltimateSpiderMan() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF),
                        new BecomeColorlessUntilEndOfTurnEffect(false)
                ),
                "Camouflage — {2}: Put a +1/+1 counter on Ultimate Spider-Man. He gains hexproof and becomes colorless until end of turn."
        ));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new DoubleCountersOnEachControlledPermanentEffect(SPIDER_OR_LEGENDARY_CREATURE));
    }
}
