package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "16")
public class EarthKingdomJailer extends Card {

    public EarthKingdomJailer() {
        PermanentPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsEnchantmentPredicate()
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentMinManaValuePredicate(3)
        ));
        target(new PermanentPredicateTargetFilter(
                targetPredicate,
                "Target must be an artifact, creature, or enchantment an opponent controls with mana value 3 or greater"
        ), 0, 1).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTargetPermanentUntilSourceLeavesEffect(false, targetPredicate));
    }
}
