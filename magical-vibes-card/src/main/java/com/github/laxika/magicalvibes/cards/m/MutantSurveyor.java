package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "98")
public class MutantSurveyor extends Card {

    public MutantSurveyor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostSelfEffect(1, 1)),
                "{2}: This creature gets +1/+1 until end of turn."
        ));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new ExileSelfFromGraveyardCost(), new DrawCardEffect(1)),
                "Max speed — {3}, Exile this card from your graveyard: Draw a card."
        ).withActivationCondition(new MaxSpeed(), "Activate only if you have max speed"));
    }
}
