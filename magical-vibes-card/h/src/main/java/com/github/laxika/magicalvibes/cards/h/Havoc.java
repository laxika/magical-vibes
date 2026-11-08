package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "181")
public class Havoc extends Card {

    public Havoc() {
        // Whenever an opponent casts a white spell, they lose 2 life.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new LoseLifeEffect(2, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
