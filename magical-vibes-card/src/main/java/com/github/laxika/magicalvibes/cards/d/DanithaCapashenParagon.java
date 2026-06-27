package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "12")
public class DanithaCapashenParagon extends Card {

    public DanithaCapashenParagon() {
        // First strike, vigilance, lifelink — loaded from Scryfall
        // Aura and Equipment spells you cast cost {1} less to cast
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.AURA),
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT)
                )), 1, CostModificationScope.SELF));
    }
}
