package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "123")
@CardRegistration(set = "MSH", collectorNumber = "125")
public class BlazingCrescendo extends Card {

    public BlazingCrescendo() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 1))
                .addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(1, false)
                        .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
