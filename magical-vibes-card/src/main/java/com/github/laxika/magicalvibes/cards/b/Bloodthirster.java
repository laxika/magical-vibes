package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackPlayerAlreadyAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "40K", collectorNumber = "73")
public class Bloodthirster extends Card {

    public Bloodthirster() {
        addEffect(EffectSlot.STATIC, new CantAttackPlayerAlreadyAttackedThisTurnEffect());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new UntapPermanentsEffect(TapUntapScope.SELF));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new AdditionalCombatPhaseEffect(1));
    }
}
