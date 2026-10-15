package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayLandsIfPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "EXO", collectorNumber = "10")
public class LimitedResources extends Card {

    public LimitedResources() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachPlayerSacrificesDownToCountEffect(5, new PermanentIsLandPredicate()));
        addEffect(EffectSlot.STATIC,
                new PlayersCantPlayLandsIfPermanentCountEffect(10, new PermanentIsLandPredicate()));
    }
}
