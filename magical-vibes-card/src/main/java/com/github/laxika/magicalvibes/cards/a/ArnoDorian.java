package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "47")
@CardRegistration(set = "ACR", collectorNumber = "139")
public class ArnoDorian extends Card {

    public ArnoDorian() {
        addMorph("{B}{R}");
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ASSASSIN))));
    }
}
