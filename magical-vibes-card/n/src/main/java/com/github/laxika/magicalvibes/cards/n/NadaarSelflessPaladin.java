package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;

@CardRegistration(set = "AFR", collectorNumber = "27")
public class NadaarSelflessPaladin extends Card {

    public NadaarSelflessPaladin() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new VentureIntoDungeonEffect());
        addEffect(EffectSlot.ON_ATTACK, new VentureIntoDungeonEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCompletedDungeon(),
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES)));
    }
}
