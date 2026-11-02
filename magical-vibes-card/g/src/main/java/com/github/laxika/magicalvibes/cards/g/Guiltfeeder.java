package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "68")
@CardRegistration(set = "C16", collectorNumber = "112")
public class Guiltfeeder extends Card {

    public Guiltfeeder() {
        addTrigger(Triggers.attacksUnblocked(),
                new LoseLifeEffect(new CardsInGraveyard(null, CountScope.TARGET_PLAYER),
                        LoseLifeRecipient.TARGET_PLAYER));
    }
}
