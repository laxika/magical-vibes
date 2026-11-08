package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "171")
@CardRegistration(set = "USG", collectorNumber = "170")
public class YawgmothsEdict extends Card {

    public YawgmothsEdict() {
        // Whenever an opponent casts a white spell, that player loses 1 life and you gain 1 life.
        addTrigger(Triggers.opponentCastsSpell().withFilter(new CardColorPredicate(CardColor.WHITE)),
                new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER),
                new GainLifeEffect(1));
    }
}
