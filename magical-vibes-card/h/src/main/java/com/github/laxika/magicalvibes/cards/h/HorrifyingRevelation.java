package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "MBS", collectorNumber = "45")
public class HorrifyingRevelation extends Card {

    public HorrifyingRevelation() {
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
        addSpellEffect(new MillEffect(1, MillRecipient.TARGET_PLAYER));
    }
}
