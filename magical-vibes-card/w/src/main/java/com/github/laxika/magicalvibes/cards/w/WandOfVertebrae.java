package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "242")
public class WandOfVertebrae extends Card {

    public WandOfVertebrae() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(1, MillRecipient.CONTROLLER)),
                "{T}: Mill a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new ExileSelfCost(),
                        new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 5, false, true)
                ),
                "{2}, {T}, Exile this artifact: Shuffle up to five target cards from your graveyard into your library."
        ));
    }
}
