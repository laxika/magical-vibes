package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "134")
public class DarkDwellerOracle extends Card {

    public DarkDwellerOracle() {
        // {1}, Sacrifice a creature: Exile the top card of your library. You may play that card this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new SacrificeCreatureCost(),
                        new ExileTopCardMayPlayThisTurnEffect(false)
                ),
                "{1}, Sacrifice a creature: Exile the top card of your library. You may play that card this turn."
        ));
    }
}
