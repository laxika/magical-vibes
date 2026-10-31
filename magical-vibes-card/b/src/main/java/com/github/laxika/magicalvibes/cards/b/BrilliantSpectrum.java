package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "BFZ", collectorNumber = "70")
public class BrilliantSpectrum extends Card {

    public BrilliantSpectrum() {
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
    }
}
