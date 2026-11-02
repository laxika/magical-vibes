package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastPermanentFromHandWithManaValueEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "177")
@CardRegistration(set = "KHM", collectorNumber = "364")
public class InSearchOfGreatness extends Card {

    public InSearchOfGreatness() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayCastPermanentFromHandWithManaValueEffect());
    }
}
