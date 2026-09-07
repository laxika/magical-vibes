package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "61")
@CardRegistration(set = "LCI", collectorNumber = "364")
public class KitesailLarcenist extends Card {

    public KitesailLarcenist() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);

        PermanentPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));

        target(new PermanentPredicateTargetFilter(targetFilter,
                "Target must be another artifact or creature"), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new SetCardTypesEffect(Set.of(CardType.ARTIFACT), GrantScope.TARGET,
                                EffectDuration.WHILE_SOURCE_REMAINS))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.TREASURE,
                                EffectDuration.WHILE_SOURCE_REMAINS))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new LosesAllAbilitiesEffect(GrantScope.TARGET,
                                EffectDuration.WHILE_SOURCE_REMAINS))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantActivatedAbilityEffect(
                                new ActivatedAbility(
                                        true,
                                        null,
                                        List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                                        "{T}, Sacrifice this artifact: Add one mana of any color."
                                ),
                                GrantScope.TARGET,
                                null,
                                EffectDuration.WHILE_SOURCE_REMAINS));
    }
}
