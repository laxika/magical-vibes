package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "60")
public class GlyphOfDelusion extends Card {

    public GlyphOfDelusion() {
        setMultiTargetConstraint(MultiTargetConstraint.BLOCKED_BY_FIRST_TARGET);

        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.WALL),
                "First target must be a Wall"));
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.GLYPH, new TargetPower()))
                .addEffect(EffectSlot.SPELL,
                        new GrantEffectToTargetEffect(EffectSlot.STATIC,
                                new DoesntUntapWithCounterEffect(CounterType.GLYPH)))
                .addEffect(EffectSlot.SPELL,
                        new GrantEffectToTargetEffect(EffectSlot.UPKEEP_TRIGGERED,
                                new RemoveCounterFromSourceEffect(CounterType.GLYPH, 1)));
    }
}
