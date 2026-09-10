package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TMT", collectorNumber = "135")
@CardRegistration(set = "TMT", collectorNumber = "275")
public class TurtlePower extends Card {

    public TurtlePower() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.TURTLE)));
    }
}
