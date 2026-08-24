package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SUM", collectorNumber = "88")
public class VesuvanDoppelganger extends Card {

    public VesuvanDoppelganger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", Set.of(),
                Map.of(EffectSlot.UPKEEP_TRIGGERED, List.of(
                        new BecomeCopyOfTargetCreatureEffect(EffectSlot.UPKEEP_TRIGGERED, false))),
                false
        ));
    }
}
