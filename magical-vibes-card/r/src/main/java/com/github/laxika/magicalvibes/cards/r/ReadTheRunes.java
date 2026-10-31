package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentUnlessDiscardEffect;

@CardRegistration(set = "ONS", collectorNumber = "104")
@CardRegistration(set = "C16", collectorNumber = "95")
public class ReadTheRunes extends Card {

    public ReadTheRunes() {
        addSpellEffect(new DrawCardEffect(new XValue()));
        addSpellEffect(EachPlayerSacrificesPermanentUnlessDiscardEffect.forEachCardDrawn());
    }
}
