package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCantAttackUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "126")
public class EvilEyeOfOrmsByGore extends Card {

    public EvilEyeOfOrmsByGore() {
        addEffect(EffectSlot.STATIC, new ControlledCreaturesCantAttackUnlessPredicateEffect(
                new PermanentHasSubtypePredicate(CardSubtype.EYE)));
        addEffect(EffectSlot.STATIC, new CanBeBlockedOnlyByFilterEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WALL),
                "Walls"));
    }
}
