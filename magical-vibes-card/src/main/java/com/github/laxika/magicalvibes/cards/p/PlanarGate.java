package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "290")
public class PlanarGate extends Card {

    public PlanarGate() {
        // Creature spells you cast cost {2} less to cast.
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardTypePredicate(CardType.CREATURE), 2, CostModificationScope.SELF));
    }
}
