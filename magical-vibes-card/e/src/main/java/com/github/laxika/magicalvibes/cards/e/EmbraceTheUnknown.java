package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "OTC", collectorNumber = "28")
@CardRegistration(set = "OTC", collectorNumber = "64")
public class EmbraceTheUnknown extends Card {

    public EmbraceTheUnknown() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
        addCastingOption(new Retrace());
    }
}
