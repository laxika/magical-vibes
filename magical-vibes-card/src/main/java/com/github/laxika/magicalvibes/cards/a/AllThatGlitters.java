package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "2")
@CardRegistration(set = "CMM", collectorNumber = "622")
@CardRegistration(set = "CMM", collectorNumber = "9")
@CardRegistration(set = "SLZ", collectorNumber = "1")
@CardRegistration(set = "SLZ", collectorNumber = "122")
@CardRegistration(set = "SLZ", collectorNumber = "243")
public class AllThatGlitters extends Card {

    public AllThatGlitters() {
        PermanentCount artifactsAndEnchantmentsYouControl = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()
                )), CountScope.CONTROLLER);
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                artifactsAndEnchantmentsYouControl,
                artifactsAndEnchantmentsYouControl,
                GrantScope.ENCHANTED_CREATURE));
    }
}
