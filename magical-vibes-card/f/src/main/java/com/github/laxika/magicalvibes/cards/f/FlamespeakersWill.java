package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "95")
public class FlamespeakersWill extends Card {

    public FlamespeakersWill() {
        target(TargetFilters.creatureYouControl())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));

        target(TargetFilters.artifact())
                .addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                        new SacrificeSelfThenEffect(new DestroyTargetPermanentEffect(
                                new PermanentIsArtifactPredicate())),
                        "Sacrifice Flamespeaker's Will to destroy target artifact?"));
    }
}
