package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "HML", collectorNumber = "87a")
@CardRegistration(set = "HML", collectorNumber = "87b")
public class FolkOfAnHavva extends Card {

    public FolkOfAnHavva() {
        // Whenever this creature blocks, it gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(2, 0), TriggerMode.ONCE_PER_BLOCK);
    }
}
