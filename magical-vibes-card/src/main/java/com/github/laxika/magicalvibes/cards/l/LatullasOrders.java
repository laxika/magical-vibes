package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "96")
public class LatullasOrders extends Card {

    public LatullasOrders() {
        target(TargetFilters.creature())
                // Whenever enchanted creature deals combat damage to defending player, you may
                // destroy target artifact that player controls.
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
                        new DestroyPermanentDamagedPlayerControlsEffect(
                                new PermanentIsArtifactPredicate(), 0),
                        "You may destroy target artifact that player controls."));
    }
}
