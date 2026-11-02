package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "162")
public class IntellectDevourer extends Card {

    public IntellectDevourer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                EachOpponentExilesFromHandEffect.withReturnOnSourceLeave(1));
        addStaticEffect(new AllowCastFromCardsExiledWithSourceEffect(true));
    }
}
