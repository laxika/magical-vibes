package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "197")
public class CustodyBattle extends Card {

    public CustodyBattle() {
        target(TargetFilters.creature()).addEffect(
                EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                        List.of(new TargetPlayerGainsControlOfEnchantedPermanentEffect()),
                        true));
    }
}
