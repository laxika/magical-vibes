package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MOM", collectorNumber = "83")
@CardRegistration(set = "MOM", collectorNumber = "354")
public class TranscendentMessage extends Card {

    public TranscendentMessage() {
        addSpellEffect(new DrawCardEffect(new XValue()));
    }
}
