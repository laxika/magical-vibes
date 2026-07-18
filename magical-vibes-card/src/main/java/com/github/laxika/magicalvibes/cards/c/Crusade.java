package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "13")
@CardRegistration(set = "5ED", collectorNumber = "22")
@CardRegistration(set = "4ED", collectorNumber = "20")
public class Crusade extends Card {

    public Crusade() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));
    }
}
