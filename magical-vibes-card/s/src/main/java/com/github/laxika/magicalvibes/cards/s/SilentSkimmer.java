package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "96")
public class SilentSkimmer extends Card {

    public SilentSkimmer() {
        addTrigger(Triggers.attacks(), new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER));
    }
}
