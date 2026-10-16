package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachColorToGraveyardEffect;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "204")
public class LotuslightDancers extends Card {

    public LotuslightDancers() {
        // When this creature enters, search your library for a black card, a green card, and a blue card.
        // Put those cards into your graveyard, then shuffle.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryForOneCardOfEachColorToGraveyardEffect(
                List.of(CardColor.BLACK, CardColor.GREEN, CardColor.BLUE)));
    }
}
