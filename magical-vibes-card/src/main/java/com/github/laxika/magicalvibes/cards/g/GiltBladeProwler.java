package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "86")
public class GiltBladeProwler extends Card {

    public GiltBladeProwler() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new PayLifeCost(1), new DrawCardEffect(1)),
                "{1}, {T}, Pay 1 life: Draw a card. Activate only if you've discarded a card this turn."
        ).withActivationCondition(
                new ControllerDiscardedCardThisTurn(),
                "Activate only if you've discarded a card this turn."));
    }
}
