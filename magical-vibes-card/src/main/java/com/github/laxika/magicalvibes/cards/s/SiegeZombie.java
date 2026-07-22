package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "131")
public class SiegeZombie extends Card {

    public SiegeZombie() {
        // Tap three untapped creatures you control: Each opponent loses 1 life.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new TapMultiplePermanentsCost(3, new PermanentIsCreaturePredicate()),
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT)
                ),
                "Tap three untapped creatures you control: Each opponent loses 1 life."
        ));
    }
}
