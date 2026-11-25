package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "77")
public class TheUnagiOfKyoshiIsland extends Card {

    public TheUnagiOfKyoshiIsland() {
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessWaterbends(4));
        addTrigger(Triggers.opponentDrawsSecondCardEachTurn(),
                new DrawCardEffect(2));
    }
}
