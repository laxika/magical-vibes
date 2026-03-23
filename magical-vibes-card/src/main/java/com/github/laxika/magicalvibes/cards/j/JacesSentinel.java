package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "XLN", collectorNumber = "283")
public class JacesSentinel extends Card {

    public JacesSentinel() {
        // As long as you control a Jace planeswalker, this creature gets +1/+0 and can't be blocked.
        addEffect(EffectSlot.STATIC, new ControlsSubtypeConditionalEffect(
                CardSubtype.JACE,
                new StaticBoostEffect(1, 0, GrantScope.SELF)
        ));
        addEffect(EffectSlot.STATIC, new ControlsSubtypeConditionalEffect(
                CardSubtype.JACE,
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)
        ));
    }
}
