package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "123")
public class RuinousPath extends Card {

    @Override
    public int getMinTargetsWhenCastForAlternateCost() {
        return 2;
    }

    public RuinousPath() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{B}{B}"))));

        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                "Target must be a creature or planeswalker"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        target(TargetFilters.landYouControl(), 0, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CastForAlternateCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CastForAlternateCost(),
                        new AnimatePermanentsEffect(
                                0, 0,
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.HASTE),
                                null,
                                Set.of(CardType.CREATURE),
                                GrantScope.TARGET,
                                EffectDuration.PERMANENT)));
    }
}
