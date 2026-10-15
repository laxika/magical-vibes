package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllNonartifactCreaturesShareColor;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "MMQ", collectorNumber = "13")
public class CommonCause extends Card {

    public CommonCause() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllNonartifactCreaturesShareColor(),
                new StaticBoostEffect(2, 2, GrantScope.ALL_CREATURES,
                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()))));
    }
}
