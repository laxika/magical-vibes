package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "144")
public class InfectedVermin extends Card {

    public InfectedVermin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new MassDamageEffect(1, true)),
                "{2}{B}: This creature deals 1 damage to each creature and each player."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(new MassDamageEffect(3, true)),
                "Threshold — {3}{B}: This creature deals 3 damage to each creature and each player."
        ).withActivationCondition(
                new GraveyardCardThreshold(7, null),
                "Activate only if there are seven or more cards in your graveyard."
        ));
    }
}
