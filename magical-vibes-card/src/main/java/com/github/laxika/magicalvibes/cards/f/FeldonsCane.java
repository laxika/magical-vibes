package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "368")
public class FeldonsCane extends Card {

    public FeldonsCane() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfCost(), new ShuffleGraveyardIntoLibraryEffect(false)),
                "{T}, Exile Feldon's Cane: Shuffle your graveyard into your library."
        ));
    }
}
