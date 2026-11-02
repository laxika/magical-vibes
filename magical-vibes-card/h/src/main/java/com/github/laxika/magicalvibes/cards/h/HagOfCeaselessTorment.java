package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "H17")
public class HagOfCeaselessTorment extends Card {

    public HagOfCeaselessTorment() {
        // At the beginning of your upkeep, each opponent loses 3 life unless that player sacrifices a
        // nonland permanent or discards a card.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), TormentOfHailfireEffect.once(3));
    }
}
