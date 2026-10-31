package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ELD", collectorNumber = "42")
public class DidntSayPlease extends Card {

    public DidntSayPlease() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new MillEffect(3, MillRecipient.TARGET_SPELL_CONTROLLER));
    }
}
