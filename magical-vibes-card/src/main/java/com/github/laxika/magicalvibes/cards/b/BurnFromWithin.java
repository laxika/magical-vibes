package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect;

@CardRegistration(set = "SOI", collectorNumber = "148")
public class BurnFromWithin extends Card {

    public BurnFromWithin() {
        addEffect(EffectSlot.SPELL,
                new DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect(
                        new XValue(), Keyword.INDESTRUCTIBLE, true));
    }
}
