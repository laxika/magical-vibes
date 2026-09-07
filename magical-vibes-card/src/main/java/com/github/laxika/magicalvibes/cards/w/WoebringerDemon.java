package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "113")
public class WoebringerDemon extends Card {

    public WoebringerDemon() {
        // EACH_UPKEEP_TRIGGERED stores the active player in targetId; route the mandatory
        // sacrifice to that player and sacrifice this creature only when they cannot pay it.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new SacrificePermanentCost(
                                new PermanentIsCreaturePredicate(), "Sacrifice a creature", false),
                        List.of(new SacrificeSelfEffect()),
                        false, false, true, false, List.of()));
    }
}
