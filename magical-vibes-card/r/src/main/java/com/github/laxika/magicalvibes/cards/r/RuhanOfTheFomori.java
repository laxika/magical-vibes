package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseRandomOpponentMustAttackThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SourceMustAttackRandomOpponentThisCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1695")
@CardRegistration(set = "CMD", collectorNumber = "221")
public class RuhanOfTheFomori extends Card {

    public RuhanOfTheFomori() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ChooseRandomOpponentMustAttackThisCombatEffect());
    }
}
