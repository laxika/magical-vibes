package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.CardsLeftGraveyardAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.condition.ControllerSacrificedPermanentSubtypeAtLeastThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "85")
public class BonecacheOverseer extends Card {

    public BonecacheOverseer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new DrawCardEffect()),
                "{T}, Pay 1 life: Draw a card. Activate only if three or more cards left your graveyard this turn or if you've sacrificed a Food this turn."
        ).withActivationCondition(
                new AnyOf(List.of(
                        new CardsLeftGraveyardAtLeastThisTurn(3),
                        new ControllerSacrificedPermanentSubtypeAtLeastThisTurn(1, CardSubtype.FOOD)
                )),
                "Activate only if three or more cards left your graveyard this turn or if you've sacrificed a Food this turn"
        ));
    }
}
