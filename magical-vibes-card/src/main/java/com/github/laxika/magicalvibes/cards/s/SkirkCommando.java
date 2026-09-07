package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "228")
public class SkirkCommando extends Card {

    public SkirkCommando() {
        addMorph("{2}{R}");
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new DealDamageToTargetCreatureDamagedPlayerControlsEffect(2),
                        "You may have Skirk Commando deal 2 damage to target creature that player controls."
                ));
    }
}
