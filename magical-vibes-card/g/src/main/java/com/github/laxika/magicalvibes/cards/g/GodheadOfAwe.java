package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "SHM", collectorNumber = "142")
public class GodheadOfAwe extends Card {

    public GodheadOfAwe() {
        // Other creatures have base power and toughness 1/1 (the source is excluded by the
        // base-P/T layered pass, so ALL_CREATURES == "other creatures" here).
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(1, 1, GrantScope.ALL_CREATURES));
    }
}
