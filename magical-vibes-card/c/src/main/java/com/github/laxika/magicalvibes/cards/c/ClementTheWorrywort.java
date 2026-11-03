package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueLessThanSourceManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "209")
public class ClementTheWorrywort extends Card {

    public ClementTheWorrywort() {
        PermanentPredicate bounceFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentManaValueLessThanSourceManaValuePredicate(true)));

        target(new PermanentPredicateTargetFilter(
                bounceFilter,
                "Target must be a creature you control with lesser mana value"), 0, 1)
                .addTriggeredAbility(new TriggeredAbility(List.of(
                                new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                                new TriggerCondition(Triggers.enters(Triggers.CREATURE,
                                        ObjectRelation.ANOTHER_YOU_CONTROL))),
                        TriggerOptions.DEFAULT, List.of(new EffectRegistration(ReturnToHandEffect.target()))));

        PermanentPredicate frogFilter = new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.FROG));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardRestrictedManaEffect(
                                ManaColor.GREEN, 1, new ManaRestriction.CreatureSpells())),
                        "{T}: Add {G}. Spend this mana only to cast a creature spell."),
                GrantScope.ALL_OWN_CREATURES,
                frogFilter));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardRestrictedManaEffect(
                                ManaColor.BLUE, 1, new ManaRestriction.CreatureSpells())),
                        "{T}: Add {U}. Spend this mana only to cast a creature spell."),
                GrantScope.ALL_OWN_CREATURES,
                frogFilter));
    }
}
