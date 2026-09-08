package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerOwnTurnCountAtMost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;

@CardRegistration(set = "M13", collectorNumber = "33")
@CardRegistration(set = "TSP", collectorNumber = "40")
public class SerraAvenger extends Card {

    public SerraAvenger() {
        // "You can't cast Serra Avenger during your first, second, or third turns of the game."
        // Only the controller's own turns count, so the restriction lapses on an opponent's turn.
        // (Flying and vigilance are auto-loaded.)
        setCastCondition(new NotCondition(new ControllerOwnTurnCountAtMost(3)));
    }
}
