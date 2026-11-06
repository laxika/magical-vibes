package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M21", collectorNumber = "180")
@CardRegistration(set = "M21", collectorNumber = "374")
public class FelineSovereign extends Card {

    public FelineSovereign() {
        PermanentHasSubtypePredicate cats = new PermanentHasSubtypePredicate(CardSubtype.CAT);

        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, cats));
        addStaticEffect(new GrantEffectEffect(
                new ProtectionFromSubtypesEffect(Set.of(CardSubtype.DOG)), GrantScope.OWN_CREATURES, cats));

        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentControlledByTriggeringPlayerPredicate())),
                "Target must be an artifact or enchantment that player controls"), 0, 1)
                .addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(cats),
                        TriggerOptions.ONE_OR_MORE,
                        new DestroyTargetPermanentEffect());
    }
}
