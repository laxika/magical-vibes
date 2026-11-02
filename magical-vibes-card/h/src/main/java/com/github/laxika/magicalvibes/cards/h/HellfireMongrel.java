package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "130")
@CardRegistration(set = "DDH", collectorNumber = "48")
public class HellfireMongrel extends Card {

    public HellfireMongrel() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new DealDamageIfFewCardsInHandEffect(2, 2));
    }
}
