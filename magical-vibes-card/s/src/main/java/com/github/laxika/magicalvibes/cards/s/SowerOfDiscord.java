package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.OtherChosenPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "187")
@CardRegistration(set = "CMM", collectorNumber = "522")
@CardRegistration(set = "C18", collectorNumber = "19")
public class SowerOfDiscord extends Card {

    public SowerOfDiscord() {
        addStaticEffect(new ChoosePlayerOnEnterEffect(2));
        addTrigger(Triggers.sourceDealsDamage(), new OtherChosenPlayerLosesLifeEffect());
    }
}
