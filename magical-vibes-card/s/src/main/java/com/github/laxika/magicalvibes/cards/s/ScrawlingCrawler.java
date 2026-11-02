package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "132")
@CardRegistration(set = "FDN", collectorNumber = "354")
@CardRegistration(set = "FDN", collectorNumber = "415")
@CardRegistration(set = "FDN", collectorNumber = "486")
@CardRegistration(set = "SLD", collectorNumber = "2399")
@CardRegistration(set = "FDC", collectorNumber = "282")
public class ScrawlingCrawler extends Card {

    public ScrawlingCrawler() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new EachPlayerDrawsCardEffect(1));
        addEffect(EffectSlot.ON_OPPONENT_DRAWS, new LoseLifeEffect(1, LoseLifeRecipient.TRIGGERING_PLAYER));
    }
}
