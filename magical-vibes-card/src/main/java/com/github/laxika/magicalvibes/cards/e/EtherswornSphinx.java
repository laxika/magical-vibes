package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MH2", collectorNumber = "195")
public class EtherswornSphinx extends Card {

    public EtherswornSphinx() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
