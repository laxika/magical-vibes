package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOM", collectorNumber = "97")
public class MeltTerrain extends Card {

    public MeltTerrain() {
        // Destroy target land. Melt Terrain deals 2 damage to that land's controller.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(),
                "Target must be a land"
        ))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER),
                        ThenEffectRecipient.TARGET_CONTROLLER_AS_TARGET));
    }
}
