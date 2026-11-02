package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "211")
public class ForgingTheTyriteSword extends Card {

    public ForgingTheTyriteSword() {
        addTrigger(Triggers.chapter(1), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.chapter(2), CreateTokenEffect.ofTreasureToken(1));
        addTrigger(Triggers.chapter(3), new SearchLibraryEffect(new CardAnyOfPredicate(List.of(
                new CardNamedPredicate("Halvar, God of Battle"),
                new CardSubtypePredicate(CardSubtype.EQUIPMENT)
        )), LibrarySearchDestination.HAND));
    }
}
