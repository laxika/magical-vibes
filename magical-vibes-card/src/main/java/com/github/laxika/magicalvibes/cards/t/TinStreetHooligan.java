package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "78")
public class TinStreetHooligan extends Card {

    public TinStreetHooligan() {
        target(TargetFilters.artifact()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN),
                new DestroyTargetPermanentEffect()
        ));
    }
}
