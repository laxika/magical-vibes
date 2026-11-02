package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesDownToCountEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPlayLandsIfPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "10")
public class LimitedResources extends Card {

    public LimitedResources() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerSacrificesDownToCountEffect(5, new PermanentIsLandPredicate()));
        addStaticEffect(new PlayersCantPlayLandsIfPermanentCountEffect(10, new PermanentIsLandPredicate()));
    }
}
