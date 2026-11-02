package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HML", collectorNumber = "87a")
@CardRegistration(set = "HML", collectorNumber = "87b")
public class FolkOfAnHavva extends Card {

    public FolkOfAnHavva() {
        // Whenever this creature blocks, it gets +2/+0 until end of turn.
        addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, new BoostSelfEffect(2, 0));
    }
}
