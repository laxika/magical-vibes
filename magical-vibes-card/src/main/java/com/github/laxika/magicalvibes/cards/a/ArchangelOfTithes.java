package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsAttacking;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToBlockEffect;

@CardRegistration(set = "ORI", collectorNumber = "4")
public class ArchangelOfTithes extends Card {

    public ArchangelOfTithes() {
        // While untapped, attacking its controller (or their planeswalkers) costs {1} per attacker.
        addEffect(EffectSlot.STATIC, new RequirePaymentToAttackEffect(1, new SourceUntapped()));
        // While attacking, blocking costs {1} per declared blocker.
        addEffect(EffectSlot.STATIC, new RequirePaymentToBlockEffect(1, new SourceIsAttacking()));
    }
}
