package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.LifeLostThisTurn;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "8")
public class ChildrenOfKorlis extends Card {

    public ChildrenOfKorlis() {
        // Sacrifice this creature: You gain life equal to the life you've lost this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(),
                        new GainLifeEffect(new LifeLostThisTurn(CountScope.CONTROLLER))),
                "Sacrifice Children of Korlis: You gain life equal to the life you've lost this turn."
        ));
    }
}
