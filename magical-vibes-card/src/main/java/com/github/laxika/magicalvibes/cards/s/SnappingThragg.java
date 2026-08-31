package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "233")
public class SnappingThragg extends Card {

    public SnappingThragg() {
        addMorph("{4}{R}{R}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new DealDamageToTargetCreatureDamagedPlayerControlsEffect(3),
                        "You may have Snapping Thragg deal 3 damage to target creature that player controls."));
    }
}
