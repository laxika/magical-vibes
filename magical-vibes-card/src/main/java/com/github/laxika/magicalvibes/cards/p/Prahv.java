package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ControllerCantAttackIfCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCantCastSpellsIfAttackedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "OPC2", collectorNumber = "31")
public class Prahv extends Card {

    public Prahv() {
        addEffect(EffectSlot.STATIC, new ControllerCantAttackIfCastSpellThisTurnEffect());
        addEffect(EffectSlot.STATIC, new ControllerCantCastSpellsIfAttackedThisTurnEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GainLifeEffect(new CardsInHand(CountScope.CONTROLLER)));
    }
}
