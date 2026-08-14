package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "FDN", collectorNumber = "132")
public class ScrawlingCrawler extends Card {

    public ScrawlingCrawler() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EachPlayerDrawsCardEffect(1));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
    }
}
