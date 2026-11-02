package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "44")
public class ThoughtweftTrio extends Card {

    public ThoughtweftTrio() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChampionCreatureEffect(CardSubtype.KITHKIN));
        addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
    }
}
