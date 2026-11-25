package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "275")
@CardRegistration(set = "WTH", collectorNumber = "134")
public class LlanowarSentinel extends Card {

    public LlanowarSentinel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.youMayPay("{1}{G}", new SearchLibraryEffect(new CardNamedPredicate("Llanowar Sentinel"), LibrarySearchDestination.BATTLEFIELD))
                .withPrompt("Pay {1}{G} to search your library for a card named Llanowar Sentinel and put it onto the battlefield?"));
    }
}
