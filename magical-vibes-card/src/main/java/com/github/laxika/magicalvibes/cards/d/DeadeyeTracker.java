package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardsFromOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "99")
public class DeadeyeTracker extends Card {

    public DeadeyeTracker() {
        // {1}{B}, {T}: Exile two target cards from an opponent's graveyard. Deadeye Tracker explores.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(
                        new ExileTargetCardsFromOpponentGraveyardEffect(2),
                        new ExploreEffect()
                ),
                "{1}{B}, {T}: Exile two target cards from an opponent's graveyard. Deadeye Tracker explores."
        ));
    }
}
