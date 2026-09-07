package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.DistinctManaValuesAmongCardsInGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "60")
public class SewerCrocodile extends Card {

    public SewerCrocodile() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new ReduceActivationCostEffect(new FixedIfCondition(
                                new DistinctManaValuesAmongCardsInGraveyardAtLeast(5), 3, 0)),
                        new MakeCreatureUnblockableEffect(true)
                ),
                "{3}{U}: This creature can't be blocked this turn. This ability costs {3} less to activate "
                        + "if there are five or more mana values among cards in your graveyard."
        ));
    }
}
