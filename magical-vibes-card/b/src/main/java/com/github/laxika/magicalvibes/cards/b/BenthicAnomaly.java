package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "46")
public class BenthicAnomaly extends Card {

    public BenthicAnomaly() {
        addTrigger(Triggers.youCastThisSpell(),
                new EachOpponentChoosesCreatureCreateTokenCopyWithTotalPowerToughnessEffect());
    }
}
