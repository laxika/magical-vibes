package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "51")
@CardRegistration(set = "CMD", collectorNumber = "42")
@CardRegistration(set = "C18", collectorNumber = "84")
public class ConundrumSphinx extends Card {

    public ConundrumSphinx() {
        addTrigger(Triggers.attacks(), new EachPlayerNameCardRevealTopEffect());
    }
}
