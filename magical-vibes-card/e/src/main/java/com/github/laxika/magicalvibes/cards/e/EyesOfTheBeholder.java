package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "101")
@CardRegistration(set = "HBG", collectorNumber = "154")
public class EyesOfTheBeholder extends Card {

    public EyesOfTheBeholder() {
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-11, -11));
    }
}
