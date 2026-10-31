package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TurnsTakenByController;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "MB1", collectorNumber = "19")
public class ControlWinCondition extends Card {

    public ControlWinCondition() {
        addStaticEffect(new CantBeCounteredEffect());
        TurnsTakenByController turnsTaken = new TurnsTakenByController();
        addStaticEffect(new SetPowerToughnessToAmountEffect(turnsTaken, turnsTaken));
    }
}
