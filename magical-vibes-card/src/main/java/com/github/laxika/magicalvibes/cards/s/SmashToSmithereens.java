package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SHM", collectorNumber = "107")
public class SmashToSmithereens extends Card {

    public SmashToSmithereens() {
        // Destroy target artifact. Smash to Smithereens deals 3 damage to that artifact's controller.
        // The controller is snapshotted before destruction; the then-effect deals real damage (not life loss).
        target(TargetFilters.artifact()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                new DealDamageToPlayersEffect(3, DamageRecipient.CONTROLLER),
                ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
