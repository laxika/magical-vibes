package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "879")
public class MinscBooTimelessHeroes extends Card {

    private static final CreateTokenEffect BOO_TOKEN = new CreateTokenEffect(
            CardType.CREATURE, 1, "Boo", 1, 1, CardColor.RED, null,
            List.of(CardSubtype.HAMSTER), Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of(),
            false, false, Map.of(), List.of(), false, false, true, 0, Set.of(),
            Set.of(CardSupertype.LEGENDARY));

    public MinscBooTimelessHeroes() {
        MayEffect createBoo = new MayEffect(BOO_TOKEN, "Create Boo?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, createBoo);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, createBoo);

        PermanentPredicate creatureWithTrampleOrHaste = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasKeywordPredicate(Keyword.TRAMPLE),
                        new PermanentHasKeywordPredicate(Keyword.HASTE)))));
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 3, creatureWithTrampleOrHaste)),
                "+1: Put three +1/+1 counters on up to one target creature with trample or haste.",
                null, 1, null, null,
                List.of(new PermanentPredicateTargetFilter(creatureWithTrampleOrHaste,
                        "Target must be a creature with trample or haste")), 0, 1));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new SacrificePermanentThenEffect(
                        new PermanentIsCreaturePredicate(),
                        SequenceEffect.of(
                                new DealDamageToAnyTargetEffect(new SacrificedPermanentPower()),
                                new DrawCardIfSacrificedCardMatchesEffect(
                                        new CardSubtypePredicate(CardSubtype.HAMSTER),
                                        new SacrificedPermanentPower())),
                        "a creature")),
                "−2: Sacrifice a creature. When you do, Minsc & Boo deals X damage to any target, "
                        + "where X is that creature's power. If the sacrificed creature was a Hamster, "
                        + "draw X cards."));
    }
}
