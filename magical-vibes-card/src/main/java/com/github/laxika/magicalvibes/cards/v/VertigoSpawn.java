package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "GPT", collectorNumber = "42")
public class VertigoSpawn extends Card {

    public VertigoSpawn() {
        addEffect(EffectSlot.ON_BLOCK, new TapPermanentsEffect(TapUntapScope.TARGET));
        addEffect(EffectSlot.ON_BLOCK, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
