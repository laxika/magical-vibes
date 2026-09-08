package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "108")
public class HeraldOfHadar extends Card {

    public HeraldOfHadar() {
        LoseLifeEffect eachOpponentLosesTwo = new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT);
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}",
                List.of(new RollD20Effect(
                        eachOpponentLosesTwo,
                        SequenceEffect.of(eachOpponentLosesTwo, new GainLifeEffect(2)),
                        SequenceEffect.of(eachOpponentLosesTwo, new GainLifeEffect(2),
                                CreateTokenEffect.ofTreasureToken(2)))),
                "Circle of Death — {5}{B}: Roll a d20. 1—9: Each opponent loses 2 life. 10—19: Each opponent loses 2 life and you gain 2 life. 20: Each opponent loses 2 life and you gain 2 life. Create two Treasure tokens."
        ));
    }
}
