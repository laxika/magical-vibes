package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "275")
@CardRegistration(set = "WTH", collectorNumber = "134")
public class LlanowarSentinel extends Card {

    public LlanowarSentinel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new PayManaAndSearchLibraryForCardNamedToBattlefieldEffect("{1}{G}", "Llanowar Sentinel"),
                "Pay {1}{G} to search your library for a card named Llanowar Sentinel and put it onto the battlefield?"
        ));
    }
}
