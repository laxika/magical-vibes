package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "87")
public class ZhalfirinShapecraft extends Card {

    public ZhalfirinShapecraft() {
        target(TargetFilters.creature())
                .addSpellEffect(new SetBasePowerToughnessEffect(4, 3))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
