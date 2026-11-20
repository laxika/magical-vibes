package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HarmonizeCast;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "TDC", collectorNumber = "41")
@CardRegistration(set = "TDC", collectorNumber = "81")
public class ZenithFestival extends Card {

    public ZenithFestival() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(new XValue(), false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
        addCastingOption(new HarmonizeCast("{X}{R}{R}"));
    }
}
