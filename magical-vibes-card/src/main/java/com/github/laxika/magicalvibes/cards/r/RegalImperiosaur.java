package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DFT", collectorNumber = "177")
public class RegalImperiosaur extends Card {

    public RegalImperiosaur() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1,
                GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)));
    }
}
