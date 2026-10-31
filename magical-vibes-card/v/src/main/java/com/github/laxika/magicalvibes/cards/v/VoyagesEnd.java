package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "73")
public class VoyagesEnd extends Card {

    public VoyagesEnd() {
        target(TargetFilters.creature()).addSpellEffect(ReturnToHandEffect.target());
        addSpellEffect(new ScryEffect(1));
    }
}
