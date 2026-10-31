package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DistantMemoriesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "24")
public class DistantMemories extends Card {

    public DistantMemories() {
        addSpellEffect(new DistantMemoriesEffect());
    }
}
