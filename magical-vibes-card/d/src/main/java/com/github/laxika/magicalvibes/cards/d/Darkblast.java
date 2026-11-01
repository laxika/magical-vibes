package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "82")
@CardRegistration(set = "GK1", collectorNumber = "51")
public class Darkblast extends Card {

    public Darkblast() {
        target(TargetFilters.creature()).addSpellEffect(new BoostTargetCreatureEffect(-1, -1));
        addStaticEffect(new DredgeEffect(3));
    }
}
