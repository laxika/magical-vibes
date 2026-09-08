package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "41")
public class Archipelagore extends Card {

    public Archipelagore() {
        targetUpTo(new TimesSourceMutated(), TargetFilters.creature(), 100)
                .addEffect(EffectSlot.ON_SELF_MUTATES, new TapPermanentsEffect(TapUntapScope.TARGET))
                .addEffect(EffectSlot.ON_SELF_MUTATES, new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
