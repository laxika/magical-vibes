package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyNamedCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "YBLB", collectorNumber = "9")
public class ChitteringSkullspeaker extends Card {

    private static final String CARD_NAME = "Chittering Skullspeaker";

    public ChitteringSkullspeaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new IntensifyNamedCardsEffect(CARD_NAME));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(new SourceIntensity()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LoseLifeEffect(new SourceIntensity(), LoseLifeRecipient.CONTROLLER));
    }
}
