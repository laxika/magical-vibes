package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "28")
public class ScholarOfAthreos extends Card {

    public ScholarOfAthreos() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                "{2}{B}: Each opponent loses 1 life. You gain life equal to the life lost this way."
        ));
    }
}
