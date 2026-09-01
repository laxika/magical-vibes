package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "286")
public class MarblePriest extends Card {

    public MarblePriest() {
        PermanentHasSubtypePredicate walls = new PermanentHasSubtypePredicate(CardSubtype.WALL);

        // All Walls able to block this creature do so.
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect(walls));

        // Prevent all combat damage that would be dealt to this creature by Walls.
        addEffect(EffectSlot.STATIC, new PreventDamageToSelfFromCreaturesEffect(walls, true));
    }
}
