package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DOM", collectorNumber = "273")
public class TeferisSentinel extends Card {

    public TeferisSentinel() {
        addEffect(EffectSlot.STATIC, new ControlsSubtypeConditionalEffect(
                CardSubtype.TEFERI,
                new StaticBoostEffect(4, 0, GrantScope.SELF)
        ));
    }
}
