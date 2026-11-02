package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandAndBecomeForetoldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHC", collectorNumber = "16")
public class EtherealValkyrie extends Card {

    public EtherealValkyrie() {
        SequenceEffect drawThenForetell = SequenceEffect.of(
                new DrawCardEffect(), new ExileCardFromHandAndBecomeForetoldEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), drawThenForetell);
        addTrigger(Triggers.attacks(), drawThenForetell);
    }
}
