package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "39")
public class DrownedSecrets extends Card {

    public DrownedSecrets() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.BLUE)),
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
