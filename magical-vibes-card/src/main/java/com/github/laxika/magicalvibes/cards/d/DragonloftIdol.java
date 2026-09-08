package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.Set;

@CardRegistration(set = "DTK", collectorNumber = "237")
public class DragonloftIdol extends Card {

    public DragonloftIdol() {
        // As long as you control a Dragon, this creature gets +1/+1 and has flying and trample.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.DRAGON)),
                new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING, Keyword.TRAMPLE), GrantScope.SELF)));
    }
}
