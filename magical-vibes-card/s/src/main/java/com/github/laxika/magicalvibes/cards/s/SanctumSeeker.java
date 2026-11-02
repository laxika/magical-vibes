package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "120")
@CardRegistration(set = "LCC", collectorNumber = "209")
@CardRegistration(set = "VOC", collectorNumber = "136")
public class SanctumSeeker extends Card {

    public SanctumSeeker() {
        // Whenever a Vampire you control attacks, each opponent loses 1 life and you gain 1 life.
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.VAMPIRE),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)));
        addTrigger(Triggers.creatureYouControlAttacks(),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.VAMPIRE),
                        new GainLifeEffect(1)));
    }
}
