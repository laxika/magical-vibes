package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "167")
public class LoxodonSurveyor extends Card {

    public LoxodonSurveyor() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileSelfFromGraveyardCost(), new DrawCardEffect(1)),
                "Max speed — {3}, Exile this card from your graveyard: Draw a card."
        ).withActivationCondition(new MaxSpeed(), "Activate only if you have max speed"));
    }
}
