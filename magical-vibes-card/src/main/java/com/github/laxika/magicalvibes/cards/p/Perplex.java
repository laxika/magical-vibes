package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsHandEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "217")
public class Perplex extends Card {

    public Perplex() {
        addEffect(EffectSlot.SPELL, new CounterUnlessDiscardsHandEffect());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}{B}",
                List.of(new SearchLibraryEffect(null, LibrarySearchDestination.HAND,
                        new ManaValueBound(new LastDiscardedCardManaValue(), true, 0))),
                "Transmute {1}{U}{B} ({1}{U}{B}, Discard this card: Search your library for a card with the same mana value as this card, reveal it, put it into your hand, then shuffle. Transmute only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
