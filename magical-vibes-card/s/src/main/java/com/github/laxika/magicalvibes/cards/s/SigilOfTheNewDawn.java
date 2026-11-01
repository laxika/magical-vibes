package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "55")
@CardRegistration(set = "UMA", collectorNumber = "35")
public class SigilOfTheNewDawn extends Card {

    public SigilOfTheNewDawn() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                new MayPayManaEffect("{1}{W}", new ReturnTriggeringCardToOwnerHandEffect(),
                        "Pay {1}{W} to return that card to your hand?"));
    }
}
