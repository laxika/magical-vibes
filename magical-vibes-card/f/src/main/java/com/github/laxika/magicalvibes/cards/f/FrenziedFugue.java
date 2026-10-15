package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "C16", collectorNumber = "18")
public class FrenziedFugue extends Card {

    public FrenziedFugue() {
        target(TargetFilters.permanent());

        SequenceEffect effect = SequenceEffect.of(
                new GainControlOfEnchantedPermanentEffect(ControlDuration.END_OF_TURN),
                new UntapPermanentsEffect(TapUntapScope.ENCHANTED),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ENCHANTED_PERMANENT));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, effect);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, effect);
    }
}
