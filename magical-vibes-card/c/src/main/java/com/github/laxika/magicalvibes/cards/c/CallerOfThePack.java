package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "34")
@CardRegistration(set = "CMA", collectorNumber = "94")
public class CallerOfThePack extends Card {

    public CallerOfThePack() {
        addTrigger(Triggers.attacks(),
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
    }
}
