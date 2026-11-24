package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "78")
@CardRegistration(set = "ALL", collectorNumber = "31")
public class LibraryOfLatNam extends Card {

    public LibraryOfLatNam() {
        addSpellEffect(ChooseOneEffect.anOpponentChooses(List.of(
                new ChooseOneOption("You draw three cards at the beginning of the next turn's upkeep",
                        new RegisterDrawCardsAtNextUpkeepEffect(3)),
                new ChooseOneOption("You search your library for a card and put it into your hand",
                        new SearchLibraryEffect()))));
    }
}
