package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "105")
public class AvengingDruid extends Card {

    public AvengingDruid() {
        addTrigger(Triggers.dealsDamageToOpponent(),
                new MayEffect(new RevealUntilLandToBattlefieldRestToGraveyardEffect(),
                        "Reveal cards until you reveal a land card?"));
    }
}
