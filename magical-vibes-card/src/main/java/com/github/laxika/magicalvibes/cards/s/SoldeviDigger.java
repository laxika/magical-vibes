package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTopCardOfGraveyardOnBottomOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "131")
public class SoldeviDigger extends Card {

    public SoldeviDigger() {
        // {2}: Put the top card of your graveyard on the bottom of your library.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PutTopCardOfGraveyardOnBottomOfLibraryEffect()),
                "{2}: Put the top card of your graveyard on the bottom of your library."));
    }
}
