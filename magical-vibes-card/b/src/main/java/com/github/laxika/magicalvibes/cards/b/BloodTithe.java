package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "M11", collectorNumber = "84")
public class BloodTithe extends Card {

    public BloodTithe() {
        addSpellEffect(new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
