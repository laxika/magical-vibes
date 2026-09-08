package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "326")
@CardRegistration(set = "WAR", collectorNumber = "110")
public class VampireOpportunist extends Card {

    public VampireOpportunist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}{B}",
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2)
                ),
                "{6}{B}, {T}: Each opponent loses 2 life and you gain 2 life."
        ));
    }
}
