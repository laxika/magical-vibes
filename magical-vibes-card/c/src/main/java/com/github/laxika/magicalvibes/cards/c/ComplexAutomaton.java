package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "128")
public class ComplexAutomaton extends Card {

    public ComplexAutomaton() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsPermanentCount(7, new PermanentTruePredicate()), ReturnToHandEffect.self()));
    }
}
