package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "109")
public class GreedyFreebooter extends Card {

    public GreedyFreebooter() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ScryEffect(1),
                CreateTokenEffect.ofTreasureToken(1)));
    }
}
