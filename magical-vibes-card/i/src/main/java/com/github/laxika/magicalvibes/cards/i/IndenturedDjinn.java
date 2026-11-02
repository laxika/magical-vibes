package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayDrawUpToNCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "85")
public class IndenturedDjinn extends Card {

    public IndenturedDjinn() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachOtherPlayerMayDrawUpToNCardsEffect(3));
    }
}
