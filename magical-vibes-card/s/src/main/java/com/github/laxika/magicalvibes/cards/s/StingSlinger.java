package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "161")
public class StingSlinger extends Card {

    public StingSlinger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new BlightEffect(1, new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT))),
                "{1}{R}, {T}, Blight 1: This creature deals 2 damage to each opponent."
        ));
    }
}
