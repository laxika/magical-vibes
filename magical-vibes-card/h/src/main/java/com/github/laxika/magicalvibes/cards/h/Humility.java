package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "TMP", collectorNumber = "24")
@CardRegistration(set = "TPR", collectorNumber = "16")
public class Humility extends Card {

    public Humility() {
        // All creatures lose all abilities (layer 6) and have base power and toughness 1/1 (layer 7b).
        addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ALL_CREATURES_INCLUDING_SELF));
        addStaticEffect(new SetBasePowerToughnessEffect(1, 1, GrantScope.ALL_CREATURES_INCLUDING_SELF));
    }
}
