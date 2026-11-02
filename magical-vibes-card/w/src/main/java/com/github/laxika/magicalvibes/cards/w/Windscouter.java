package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "53")
public class Windscouter extends Card {

    public Windscouter() {
        addTrigger(Triggers.attacks(), new ReturnSelfToHandAtEndOfCombatEffect());
        addTrigger(Triggers.blocks(), new ReturnSelfToHandAtEndOfCombatEffect());
    }
}
