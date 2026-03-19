package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForSubtypeEffect;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "12")
public class DanithaCapashenParagon extends Card {

    public DanithaCapashenParagon() {
        // First strike, vigilance, lifelink — loaded from Scryfall
        // Aura and Equipment spells you cast cost {1} less to cast
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostForSubtypeEffect(
                Set.of(CardSubtype.AURA, CardSubtype.EQUIPMENT), 1));
    }
}
