package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "25")
public class HallowedHealer extends Card {

    public HallowedHealer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(2)),
                "{T}: Prevent the next 2 damage that would be dealt to any target this turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(PreventDamageEffect.nextToTarget(4)),
                "Threshold — {T}: Prevent the next 4 damage that would be dealt to any target this turn. "
                        + "Activate only if there are seven or more cards in your graveyard."
        ).withActivationCondition(
                new GraveyardCardThreshold(7, null),
                "Activate only if there are seven or more cards in your graveyard."
        ));
    }
}
