package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "105")
public class WhisperSquad extends Card {

    public WhisperSquad() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SearchLibraryEffect(
                        new CardNamedPredicate("Whisper Squad"),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{1}{B}: Search your library for a card named Whisper Squad, put it onto the battlefield tapped, then shuffle."
        ));
    }
}
