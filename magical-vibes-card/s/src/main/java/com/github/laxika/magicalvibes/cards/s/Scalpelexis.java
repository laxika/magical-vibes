package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "105")
@CardRegistration(set = "JUD", collectorNumber = "50")
public class Scalpelexis extends Card {

    public Scalpelexis() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardsRepeatOnDuplicateEffect(4));
    }
}
