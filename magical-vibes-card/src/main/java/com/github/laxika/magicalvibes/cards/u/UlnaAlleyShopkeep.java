package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOS", collectorNumber = "103")
public class UlnaAlleyShopkeep extends Card {

    public UlnaAlleyShopkeep() {
        // Menace is auto-loaded from Scryfall.
        // Infusion — This creature gets +2/+0 as long as you gained life this turn.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
