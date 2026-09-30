package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect;

@CardRegistration(set = "M3C", collectorNumber = "46")
public class BenthicAnomaly extends Card {

    public BenthicAnomaly() {
        addEffect(EffectSlot.ON_SELF_CAST,
                new EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect());
    }
}
