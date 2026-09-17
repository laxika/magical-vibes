package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;

@CardRegistration(set = "CMD", collectorNumber = "221")
public class RuhanOfTheFomori extends Card {

    public RuhanOfTheFomori() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseRandomOpponentMustAttackThisCombatEffect());
    }
}
