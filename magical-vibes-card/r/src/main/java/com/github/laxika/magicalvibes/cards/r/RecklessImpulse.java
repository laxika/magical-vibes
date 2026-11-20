package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "VOW", collectorNumber = "174")
@CardRegistration(set = "DBL", collectorNumber = "441")
public class RecklessImpulse extends Card {

    public RecklessImpulse() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
