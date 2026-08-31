package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "FUT", collectorNumber = "158")
public class SliverLegion extends Card {

    public SliverLegion() {
        addEffect(EffectSlot.STATIC, new BoostBySharedCreatureTypeEffect(
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
