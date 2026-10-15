package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;

@CardRegistration(set = "LRW", collectorNumber = "44")
public class ThoughtweftTrio extends Card {

    public ThoughtweftTrio() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChampionCreatureEffect(CardSubtype.KITHKIN));
        addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
    }
}
