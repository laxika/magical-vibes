package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingCreatureToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "TMT", collectorNumber = "78")
@CardRegistration(set = "TMT", collectorNumber = "269")
public class SouthWindAvatar extends Card {

    public SouthWindAvatar() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new GainLifeEqualToDyingCreatureToughnessEffect());
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
