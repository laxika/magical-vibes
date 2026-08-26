package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "108")
public class SulfurElemental extends Card {

    public SulfurElemental() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, -1, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
