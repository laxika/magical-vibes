package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "227")
public class PerpetualTimepiece extends Card {

    public PerpetualTimepiece() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillEffect(2, MillRecipient.CONTROLLER)),
                "{T}: Mill two cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileSelfCost(),
                        new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null)
                ),
                "{2}, Exile this artifact: Shuffle any number of target cards from your graveyard into your library."
        ));
    }
}
