package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "AVR", collectorNumber = "125")
public class Aggravate extends Card {

    public Aggravate() {
        // Aggravate deals 1 damage to each creature target player controls.
        // Each creature dealt damage this way attacks this turn if able.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(DealDamageEffect.toEachCreatureOfTargetedPlayer(1))
                .addSpellEffect(new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK,
                        Subjects.thisWay(StepKind.DEAL_DAMAGE, new PermanentIsCreaturePredicate())));
    }
}
