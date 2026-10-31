package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastAnotherSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "84")
@CardRegistration(set = "DDS", collectorNumber = "9")
public class SiftThroughSands extends Card {

    public SiftThroughSands() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(1, DiscardRecipient.CONTROLLER, false));
        addSpellEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new ControllerCastAnotherSpellThisTurn(new CardNamedPredicate("Peer Through Depths")),
                        new ControllerCastAnotherSpellThisTurn(new CardNamedPredicate("Reach Through Mists")))),
                new MayEffect(new SearchLibraryEffect(
                        new CardNamedPredicate("The Unspeakable"), LibrarySearchDestination.BATTLEFIELD),
                        "Search your library for a card named The Unspeakable?")));
    }
}
