package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedPermanentAndReattachSourceAuraEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "131")
public class NettlevineBlight extends Card {

    public NettlevineBlight() {
        // Enchant creature or land
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsLandPredicate()
                )),
                "Target must be a creature or land"
        ))
                // Enchanted permanent has "At the beginning of your end step, sacrifice this permanent
                // and attach Nettlevine Blight to a creature or land you control."
                .addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED,
                        new SacrificeEnchantedPermanentAndReattachSourceAuraEffect());
    }
}
