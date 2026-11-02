package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "12")
public class RuinProcessor extends Card {

    public RuinProcessor() {
        addTrigger(Triggers.youCastThisSpell(),
                new PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect(5));
    }
}
