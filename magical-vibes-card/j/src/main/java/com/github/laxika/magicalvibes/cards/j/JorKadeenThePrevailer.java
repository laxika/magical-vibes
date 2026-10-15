package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "NPH", collectorNumber = "128")
@CardRegistration(set = "C21", collectorNumber = "220")
@CardRegistration(set = "C16", collectorNumber = "206")
@CardRegistration(set = "ONC", collectorNumber = "119")
public class JorKadeenThePrevailer extends Card {

    public JorKadeenThePrevailer() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new Metalcraft(), new StaticBoostEffect(3, 0, GrantScope.ALL_OWN_CREATURES)));
    }
}
