package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "NCC", collectorNumber = "63")
@CardRegistration(set = "NCC", collectorNumber = "163")
public class ParkHeightsMaverick extends Card {

    public ParkHeightsMaverick() {
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentPowerAtLeastPredicate(3),
                "creatures with power 3 or greater"
        ));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ProliferateEffect());
        addEffect(EffectSlot.ON_DEATH, new ProliferateEffect());
    }
}
