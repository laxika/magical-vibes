package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "73")
public class NimShrieker extends Card {

    public NimShrieker() {
        // This creature gets +1/+0 for each artifact you control.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), new Fixed(0)));
    }
}
