package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M15", collectorNumber = "57")
public class GlacialCrasher extends Card {

    public GlacialCrasher() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new AnyPlayerControlsPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                "a Mountain on the battlefield"
        ));
    }
}
