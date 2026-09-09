package com.github.laxika.magicalvibes.cards.b;

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
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "142")
public class BoilingEarth extends Card {

    @Override
    public int getMinTargetsWhenCastForAlternateCost() {
        return 1;
    }

    public BoilingEarth() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{6}{R}"))));

        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, false, false,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

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
