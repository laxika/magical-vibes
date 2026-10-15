package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ULG", collectorNumber = "18")
public class PlanarCollapse extends Card {

    public PlanarCollapse() {
        PermanentIsCreaturePredicate creature = new PermanentIsCreaturePredicate();
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyPlayerControlsPermanentCount(4, creature),
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new DestroyAllPermanentsEffect(creature, true))));
    }
}
