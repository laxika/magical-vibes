package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "10E", collectorNumber = "179")
@CardRegistration(set = "9ED", collectorNumber = "164")
@CardRegistration(set = "8ED", collectorNumber = "165")
@CardRegistration(set = "7ED", collectorNumber = "163")
public class SoulFeast extends Card {

    public SoulFeast() {
        // Target player loses 4 life and you gain 4 life.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(4, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
