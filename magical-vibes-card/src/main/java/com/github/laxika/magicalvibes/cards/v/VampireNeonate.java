package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "124")
public class VampireNeonate extends Card {

    public VampireNeonate() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)
                ),
                "{2}, {T}: Each opponent loses 1 life and you gain 1 life."
        ));
    }
}
