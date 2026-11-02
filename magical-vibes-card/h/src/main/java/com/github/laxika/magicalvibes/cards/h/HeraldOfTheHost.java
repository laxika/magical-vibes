package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "30")
@CardRegistration(set = "C15", collectorNumber = "4")
@CardRegistration(set = "CM2", collectorNumber = "29")
public class HeraldOfTheHost extends Card {

    public HeraldOfTheHost() {
        addTrigger(Triggers.attacks(),
                new CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect(true));
    }
}
