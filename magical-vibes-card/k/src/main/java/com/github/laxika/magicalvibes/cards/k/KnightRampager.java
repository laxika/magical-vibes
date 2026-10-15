package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentEffect;

@CardRegistration(set = "40K", collectorNumber = "80")
public class KnightRampager extends Card {

    public KnightRampager() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseRandomOpponentMustAttackThisCombatEffect());
        addEffect(EffectSlot.ON_DEATH, DealDamageToRandomOpponentEffect.targeted(4));
    }
}
