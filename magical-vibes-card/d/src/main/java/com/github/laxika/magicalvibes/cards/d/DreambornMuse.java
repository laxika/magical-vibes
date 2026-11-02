package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "82")
@CardRegistration(set = "LGN", collectorNumber = "36")
@CardRegistration(set = "CMD", collectorNumber = "44")
@CardRegistration(set = "CM2", collectorNumber = "41")
public class DreambornMuse extends Card {

    public DreambornMuse() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new MillEffect(new CardsInHand(CountScope.TARGET_PLAYER), MillRecipient.ACTIVE_PLAYER));
    }
}
