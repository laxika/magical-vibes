package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M10", collectorNumber = "202")
public class Regenerate extends Card {

    public Regenerate() {
        target(TargetFilters.creature()).addSpellEffect(new RegenerateEffect(true));
    }
}
