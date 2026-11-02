package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromCardTypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TakeInitiativeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "2505")
public class SeasonedDungeoneer extends Card {

    public SeasonedDungeoneer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TakeInitiativeEffect());

        PermanentPredicate eligibleTarget = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingPredicate(),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.CLERIC),
                        new PermanentHasSubtypePredicate(CardSubtype.ROGUE),
                        new PermanentHasSubtypePredicate(CardSubtype.WARRIOR),
                        new PermanentHasSubtypePredicate(CardSubtype.WIZARD)))));
        target(new PermanentPredicateTargetFilter(
                eligibleTarget,
                "Target must be an attacking Cleric, Rogue, Warrior, or Wizard"))
                .addTrigger(Triggers.youAttack(),
                        new GrantProtectionFromCardTypeUntilEndOfTurnEffect(
                                CardType.CREATURE, eligibleTarget, TargetPredicates.creature()))
                .addTrigger(Triggers.youAttack(), new ExploreEffect(true));
    }
}
