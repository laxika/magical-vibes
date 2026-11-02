package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "240")
@CardRegistration(set = "C17", collectorNumber = "29")
public class TerritorialHellkite extends Card {

    public TerritorialHellkite() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ChooseRandomOpponentMustAttackThisCombatEffect(true, true));
    }
}
