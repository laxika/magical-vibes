package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;

@CardRegistration(set = "BRO", collectorNumber = "249")
public class SuChiCaveGuard extends Card {

    public SuChiCaveGuard() {
        addEffect(EffectSlot.ON_DEATH,
                new AwardPersistentManaEffect(ManaColor.COLORLESS, new Fixed(8)));
    }
}
