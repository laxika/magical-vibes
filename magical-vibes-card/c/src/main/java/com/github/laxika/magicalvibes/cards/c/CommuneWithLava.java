package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;

@CardRegistration(set = "DTK", collectorNumber = "131")
@CardRegistration(set = "AFC", collectorNumber = "118")
@CardRegistration(set = "C20", collectorNumber = "149")
public class CommuneWithLava extends Card {

    public CommuneWithLava() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(new XValue(), false)
                .until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN));
    }
}
