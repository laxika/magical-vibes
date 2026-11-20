package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "MOM", collectorNumber = "173")
public class WrennsResolve extends Card {

    public WrennsResolve() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
