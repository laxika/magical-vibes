package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "81")
public class VisionsOfBrutality extends Card {

    public VisionsOfBrutality() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CantBlockEffect())
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE,
                        new DamageSourceControllerLosesLifeEffect());
    }
}
