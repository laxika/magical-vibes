package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

@CardRegistration(set = "EXO", collectorNumber = "103")
@CardRegistration(set = "BTD", collectorNumber = "46")
public class SonicBurst extends Card {

    public SonicBurst() {
        // As an additional cost to cast this spell, discard a card at random.
        addCastingCost(new DiscardRandomCardCost());
        // Sonic Burst deals 4 damage to any target.
        addSpellEffect(new DealDamageToAnyTargetEffect(4));
    }
}
