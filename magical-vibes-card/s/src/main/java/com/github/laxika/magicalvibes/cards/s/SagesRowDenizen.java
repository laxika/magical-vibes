package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "GTC", collectorNumber = "46")
@CardRegistration(set = "M20", collectorNumber = "73")
public class SagesRowDenizen extends Card {

    public SagesRowDenizen() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD, new TriggeringCardConditionalEffect(
                new CardColorPredicate(CardColor.BLUE),
                new MillEffect(2, MillRecipient.TARGET_PLAYER)));
    }
}
