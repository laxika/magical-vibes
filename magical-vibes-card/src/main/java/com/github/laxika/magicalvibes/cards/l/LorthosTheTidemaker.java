package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZEN", collectorNumber = "53")
public class LorthosTheTidemaker extends Card {

    public LorthosTheTidemaker() {
        target(TargetFilters.permanent(), 0, 8)
                .addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect("{8}",
                        SequenceEffect.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                new SkipNextUntapEffect(TapUntapScope.TARGET)),
                        "Pay {8} to tap up to eight target permanents?"));
    }
}
