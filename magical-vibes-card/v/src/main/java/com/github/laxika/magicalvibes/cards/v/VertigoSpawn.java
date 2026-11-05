package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "42")
public class VertigoSpawn extends Card {

    public VertigoSpawn() {
        addTrigger(Triggers.blocks(),
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
