package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "117")
public class PutridWarrior extends Card {

    public PutridWarrior() {
        addTrigger(Triggers.dealsDamage(), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player loses 1 life.",
                        new LoseLifeEffect(1, Subjects.eachPlayer())),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player gains 1 life.",
                        new GainLifeEffect(1, Subjects.eachPlayer()))
        )));
    }
}
