package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C15", collectorNumber = "31")
@CardRegistration(set = "CM2", collectorNumber = "127")
public class WarchiefGiant extends Card {

    public WarchiefGiant() {
        addTrigger(Triggers.attacks(),
                CreateTokenCopyOfAttackingCreatureForEachOtherOpponentEffect.myriad());
    }
}
