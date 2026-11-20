package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.Card;

public class SeekTheBeast extends Card {

    public SeekTheBeast() {
        addSpellEffect(new ExileTopCardMayPlayThisTurnEffect(2, false)
                .until(Duration.UNTIL_YOUR_NEXT_END_STEP));
    }
}
