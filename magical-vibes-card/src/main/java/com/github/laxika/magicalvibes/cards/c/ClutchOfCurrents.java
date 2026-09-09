package com.github.laxika.magicalvibes.cards.c;

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
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BFZ", collectorNumber = "72")
public class ClutchOfCurrents extends Card {

    @Override
    public int getMinTargetsWhenCastForAlternateCost() {
        return 2;
    }

    public ClutchOfCurrents() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}"))));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

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
