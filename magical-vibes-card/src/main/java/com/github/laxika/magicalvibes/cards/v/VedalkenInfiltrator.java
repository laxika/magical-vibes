package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "MH2", collectorNumber = "73")
public class VedalkenInfiltrator extends Card {

    public VedalkenInfiltrator() {
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Metalcraft(),
                new StaticBoostEffect(1, 0, GrantScope.SELF)));
    }
}
