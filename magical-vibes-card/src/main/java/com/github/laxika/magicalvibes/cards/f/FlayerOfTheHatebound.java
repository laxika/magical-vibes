package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSourcePowerToAnyTargetEffect;

@CardRegistration(set = "DKA", collectorNumber = "89")
public class FlayerOfTheHatebound extends Card {

    public FlayerOfTheHatebound() {
        // Undying is auto-loaded as a keyword from Scryfall; its return mechanic is handled by the engine.
        // Whenever this creature or another creature enters from your graveyard, that creature deals
        // damage equal to its power to any target. The entering creature is the damage source.
        addEffect(EffectSlot.ON_CREATURE_ENTERS_FROM_GRAVEYARD, new DealDamageEqualToSourcePowerToAnyTargetEffect());
    }
}
