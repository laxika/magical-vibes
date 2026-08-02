package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
public class SiftThroughSands extends Card {

    public SiftThroughSands() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER, false));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AllOf(List.of(
                        new ControllerCastAnotherSpellThisTurn(new CardNamedPredicate("Peer Through Depths")),
                        new ControllerCastAnotherSpellThisTurn(new CardNamedPredicate("Reach Through Mists")))),
                new MayEffect(new SearchLibraryEffect(
                        new CardNamedPredicate("The Unspeakable"), LibrarySearchDestination.BATTLEFIELD),
                        "Search your library for a card named The Unspeakable?")));
    }
}
