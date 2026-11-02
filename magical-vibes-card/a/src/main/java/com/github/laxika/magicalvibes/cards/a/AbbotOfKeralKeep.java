package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "127")
@CardRegistration(set = "2X2", collectorNumber = "100")
@CardRegistration(set = "EA3", collectorNumber = "2")
public class AbbotOfKeralKeep extends Card {

    public AbbotOfKeralKeep() {
        // When this creature enters, exile the top card of your library. Until end of turn, you may
        // play that card. (Prowess is a Scryfall-loaded keyword.)
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileTopCardMayPlayThisTurnEffect(1, false));
    }
}
