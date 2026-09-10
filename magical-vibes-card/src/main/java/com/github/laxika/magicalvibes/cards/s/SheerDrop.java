package com.github.laxika.magicalvibes.cards.s;

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
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "48")
public class SheerDrop extends Card {

    @Override
    public int getMinTargetsWhenCastForAlternateCost() {
        return 2;
    }

    public SheerDrop() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{5}{W}"))));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate()
                )),
                "Target must be a tapped creature"))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        target(TargetFilters.landYouControl(), 0, 1)
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CastForAlternateCost(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CastForAlternateCost(),
                        new AnimatePermanentsEffect(
                                0, 0, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE), null,
                                Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.PERMANENT)));
    }
}
