package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "198")
public class KyrenNegotiations extends Card {

    public KyrenNegotiations() {
        // Tap an untapped creature you control: This enchantment deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentIsCreaturePredicate()),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                "Tap an untapped creature you control: This enchantment deals 1 damage to target player or planeswalker."));
    }
}
