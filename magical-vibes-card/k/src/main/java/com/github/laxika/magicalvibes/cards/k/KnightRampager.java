package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "80")
public class KnightRampager extends Card {

    public KnightRampager() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseRandomOpponentMustAttackThisCombatEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), DealDamageToRandomOpponentEffect.targeted(4));
    }
}
