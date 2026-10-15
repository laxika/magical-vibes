package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;

@CardRegistration(set = "40K", collectorNumber = "36")
public class HexmarkDestroyer extends Card {

    public HexmarkDestroyer() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(6));
        addUnearth("{4}{B}{B}");
    }
}
