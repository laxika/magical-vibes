package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

import java.util.List;

public class HarnfelHornOfBounty extends Card {

    public HarnfelHornOfBounty() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new ExileTopCardMayPlayThisTurnEffect(2, false)
                ),
                "Discard a card: Exile the top two cards of your library. You may play those cards this turn."
        ));
    }
}
