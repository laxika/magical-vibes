package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadXTimesThenPutCountersEffect;

@CardRegistration(set = "DSK", collectorNumber = "204")
public class ValgavothsOnslaught extends Card {

    public ValgavothsOnslaught() {
        addSpellEffect(new ManifestDreadXTimesThenPutCountersEffect(new XValue()));
    }
}
