package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.n.NezumiRoadCaptain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEO", collectorNumber = "117")
public class OkibaReckonerRaid extends Card {

    public OkibaReckonerRaid() {
        setBackFaceCard(new NezumiRoadCaptain());

        addTrigger(Triggers.chapter(1), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(1), new GainLifeEffect(1));
        addTrigger(Triggers.chapter(2), new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.chapter(2), new GainLifeEffect(1));
        addTrigger(Triggers.chapter(3), new ExileSelfAndReturnTransformedEffect(true));
    }

    @java.lang.Override
    public String getBackFaceClassName() {
        return "NezumiRoadCaptain";
    }
}
