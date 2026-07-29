package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "MIR", collectorNumber = "230")
public class MtendaLion extends Card {

    public MtendaLion() {
        // Whenever this creature attacks, defending player may pay {U}. If that player does,
        // prevent all combat damage that would be dealt by this creature this turn.
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{U}",
                PreventDamageEffect.allCombatBySelf(),
                "pay {U} to prevent all combat damage this creature would deal this turn",
                MayPayPayer.DEFENDING_PLAYER));
    }
}
