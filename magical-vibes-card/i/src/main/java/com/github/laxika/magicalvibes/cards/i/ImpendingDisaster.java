package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ULG", collectorNumber = "82")
public class ImpendingDisaster extends Card {

    public ImpendingDisaster() {
        PermanentIsLandPredicate land = new PermanentIsLandPredicate();
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerControlsPermanentCount(7, land),
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new DestroyAllPermanentsEffect(land))));
    }
}
