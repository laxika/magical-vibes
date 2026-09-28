package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "682")
public class ChaseSteinRunaway extends Card {

    public ChaseSteinRunaway() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ExileTopCardsMayPlayUntilNextTurnEffect(1)
                ),
                "{T}, Discard a card: Exile the top card of your library. Until the end of your next turn, you may play that card."
        ));
    }
}
