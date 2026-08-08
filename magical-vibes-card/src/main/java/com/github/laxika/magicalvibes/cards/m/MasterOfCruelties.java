package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CanOnlyAttackAloneEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;

@CardRegistration(set = "DGM", collectorNumber = "82")
public class MasterOfCruelties extends Card {

    public MasterOfCruelties() {
        // This creature can only attack alone.
        addEffect(EffectSlot.STATIC, new CanOnlyAttackAloneEffect());

        // Whenever this creature attacks a player and isn't blocked, that player's life total
        // becomes 1. This creature assigns no combat damage this combat. The defending player is
        // the trigger's non-targeting targetId.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new SetLifeTotalEffect(1, SetLifeTotalRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new AssignNoCombatDamageEffect());
    }
}
