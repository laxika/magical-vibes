package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;

@CardRegistration(set = "TDC", collectorNumber = "240")
@CardRegistration(set = "C17", collectorNumber = "29")
public class TerritorialHellkite extends Card {

    public TerritorialHellkite() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseRandomOpponentMustAttackThisCombatEffect(true, true));
    }
}
