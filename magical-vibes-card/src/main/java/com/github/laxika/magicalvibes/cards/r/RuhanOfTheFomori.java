package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SourceMustAttackRandomOpponentThisCombatEffect;

@CardRegistration(set = "SLD", collectorNumber = "1695")
public class RuhanOfTheFomori extends Card {

    public RuhanOfTheFomori() {
        // At the beginning of combat on your turn, choose an opponent at random. Ruhan attacks that
        // player this combat if able.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new SourceMustAttackRandomOpponentThisCombatEffect());
    }
}
