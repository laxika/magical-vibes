package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "TMP", collectorNumber = "24")
public class Humility extends Card {

    public Humility() {
        // All creatures lose all abilities (layer 6) and have base power and toughness 1/1 (layer 7b).
        // Humility itself is not a creature, so ALL_CREATURES deliberately excludes the source.
        addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ALL_CREATURES));
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(1, 1, GrantScope.ALL_CREATURES));
    }
}
