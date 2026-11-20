package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Primal Amulet — front face of Primal Amulet // Primal Wellspring.
 * {4} Artifact.
 * Instant and sorcery spells you cast cost {1} less to cast.
 * Whenever you cast an instant or sorcery spell, put a charge counter on Primal Amulet.
 * Then if there are four or more charge counters on it, you may remove those counters
 * and transform it.
 */
@CardRegistration(set = "XLN", collectorNumber = "243")
public class PrimalAmulet extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(
            List.of(new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))
    );

    public PrimalAmulet() {
        setBackFaceCard(new PrimalWellspring());

        // Instant and sorcery spells you cast cost {1} less to cast.
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                INSTANT_OR_SORCERY, 1, CostModificationScope.SELF
        ));

        // Whenever you cast an instant or sorcery spell, put a charge counter on Primal Amulet.
        // Then if there are four or more charge counters on it, you may remove those counters
        // and transform it.
        addTrigger(Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.CHARGE),
                        new ConditionalEffect(new SourceCounterThreshold(4, CounterType.CHARGE), new MayEffect(
                                SequenceEffect.of(
                                        new RemoveAllCountersEffect(CounterType.CHARGE),
                                        new TransformSelfEffect()),
                                "Remove counters and transform?"))));
    }

    @Override
    public String getBackFaceClassName() {
        return "PrimalWellspring";
    }
}
