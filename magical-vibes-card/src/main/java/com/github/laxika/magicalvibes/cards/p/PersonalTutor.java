package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "64")
@CardRegistration(set = "ME2", collectorNumber = "58")
@CardRegistration(set = "CMM", collectorNumber = "110")
@CardRegistration(set = "CMM", collectorNumber = "493")
@CardRegistration(set = "CMM", collectorNumber = "633")
@CardRegistration(set = "SLD", collectorNumber = "2492")
public class PersonalTutor extends Card {

    public PersonalTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardTypePredicate(CardType.SORCERY),
                LibrarySearchDestination.TOP_OF_LIBRARY));
    }
}
