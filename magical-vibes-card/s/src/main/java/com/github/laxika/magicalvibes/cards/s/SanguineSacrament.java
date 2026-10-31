package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryEffect;

@CardRegistration(set = "XLN", collectorNumber = "33")
public class SanguineSacrament extends Card {

    public SanguineSacrament() {
        addSpellEffect(new GainLifeEffect(new Scaled(new XValue(), 2)));
        addSpellEffect(new PutSelfOnBottomOfOwnersLibraryEffect());
    }
}
