package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

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
        // Set up back face
        PrimalWellspring backFace = new PrimalWellspring();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Instant and sorcery spells you cast cost {1} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                INSTANT_OR_SORCERY, 1, CostModificationScope.SELF
        ));

        // Whenever you cast an instant or sorcery spell, put a charge counter on Primal Amulet.
        // Then if there are four or more charge counters on it, you may remove those counters
        // and transform it.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                INSTANT_OR_SORCERY,
                List.of(new PutCounterOnSelfThenTransformIfThresholdEffect(CounterType.CHARGE, 4, true))
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "PrimalWellspring";
    }
}
