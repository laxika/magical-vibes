package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "216")
@CardRegistration(set = "LGN", collectorNumber = "105")
@CardRegistration(set = "DDK", collectorNumber = "50")
public class LavabornMuse extends Card {

    public LavabornMuse() {
        addTrigger(Triggers.upkeep(ActivePlayer.OPPONENT), new DealDamageIfFewCardsInHandEffect(2, 3));
    }
}
