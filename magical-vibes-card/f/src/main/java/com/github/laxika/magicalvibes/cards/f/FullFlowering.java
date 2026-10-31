package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PopulateNTimesEffect;

@CardRegistration(set = "C19", collectorNumber = "32")
public class FullFlowering extends Card {

    public FullFlowering() {
        addSpellEffect(new PopulateNTimesEffect(new XValue()));
    }
}
