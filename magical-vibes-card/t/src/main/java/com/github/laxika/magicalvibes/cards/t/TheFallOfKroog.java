package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "133")
public class TheFallOfKroog extends Card {

    public TheFallOfKroog() {
        SpellTarget opponentTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        SpellTarget landTarget = target(TargetFilters.land());
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);

        landTarget.addSpellEffect(new DestroyTargetPermanentEffect());
        DealDamageEffect opponentDamage =
                DealDamageEffect.to(Subjects.targetPlayer(), 3);
        DealDamageEffect creatureDamage =
                DealDamageEffect.toEachCreatureOfTargetedPlayer(1);
        opponentTarget.bindEffect(opponentDamage);
        opponentTarget.bindEffect(creatureDamage);
        addSimultaneousSpellEffects(opponentDamage, creatureDamage);
    }
}
