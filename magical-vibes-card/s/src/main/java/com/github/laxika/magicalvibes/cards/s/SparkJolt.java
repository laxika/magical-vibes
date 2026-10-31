package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "140")
public class SparkJolt extends Card {

    public SparkJolt() {
        addSpellEffect(new DealDamageToAnyTargetEffect(1));
        addSpellEffect(new ScryEffect(1));
    }
}
