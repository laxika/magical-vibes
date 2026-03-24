package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;

@CardRegistration(set = "XLN", collectorNumber = "70")
public class RiverSneak extends Card {

    public RiverSneak() {
        // River Sneak can't be blocked.
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());

        // Whenever another Merfolk you control enters, River Sneak gets +1/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new SubtypeConditionalEffect(CardSubtype.MERFOLK,
                        new BoostSelfEffect(1, 1)));
    }
}
