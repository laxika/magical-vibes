package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.condition.ControllerLostLifeLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "16")
public class PaladinOfAtonement extends Card {

    public PaladinOfAtonement() {
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new ConditionalEffect(
                new ControllerLostLifeLastTurn(),
                new PutCountersOnSourceEffect(1, 1, 1)));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(new SourceToughness()));
    }
}
