package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MSC", collectorNumber = "502")
public class HulkAlwaysAngry extends Card {

    public HulkAlwaysAngry() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate()));
        addEffect(EffectSlot.STATIC, new MustAttackEffect());
    }
}
