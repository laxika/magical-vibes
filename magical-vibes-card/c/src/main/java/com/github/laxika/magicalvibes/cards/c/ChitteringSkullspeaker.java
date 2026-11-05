package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "9")
public class ChitteringSkullspeaker extends Card {

    private static final String CARD_NAME = "Chittering Skullspeaker";

    public ChitteringSkullspeaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new IntensifyNamedCardsEffect(CARD_NAME),
                new DrawCardEffect(new SourceIntensity()),
                new LoseLifeEffect(new SourceIntensity(), LoseLifeRecipient.CONTROLLER));
    }
}
