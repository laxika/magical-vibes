package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "127")
public class AbandonThePost extends Card {

    public AbandonThePost() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
