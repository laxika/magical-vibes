package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

/**
 * Jhoira's Familiar — {4} Artifact Creature — Bird (2/2)
 *
 * Flying
 * Historic spells you cast cost {1} less to cast.
 * (Artifacts, legendaries, and Sagas are historic.)
 */
@CardRegistration(set = "DOM", collectorNumber = "220")
public class JhoirasFamiliar extends Card {

    public JhoirasFamiliar() {
        // Flying is loaded from Scryfall
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardIsHistoricPredicate(), 1, CostModificationScope.SELF
        ));
    }
}
