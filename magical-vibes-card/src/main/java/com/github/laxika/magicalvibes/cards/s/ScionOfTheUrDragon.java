package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "246")
public class ScionOfTheUrDragon extends Card {

    public ScionOfTheUrDragon() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffect()),
                "{2}: Search your library for a Dragon permanent card and put it into your graveyard. "
                        + "If you do, Scion of the Ur-Dragon becomes a copy of that card until end of turn. "
                        + "Then shuffle."));
    }
}
