package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * An attack-group effect for "whenever one or more [matching] creatures you control attack, they gain
 * [keyword] until end of turn". The matching attackers are bound when the trigger is created, so
 * "they" is fixed even if a creature stops matching before resolution; creatures that have left the
 * battlefield by resolution are skipped. The trigger is not created when no attacker matches.
 */
public record GrantKeywordToTriggeringAttackersEffect(
        PermanentPredicate attackingCreaturePredicate,
        Keyword keyword,
        List<AttackingPermanentSnapshot> attackingCreatures
) implements AttackingCreaturesAwareEffect {

    public GrantKeywordToTriggeringAttackersEffect(PermanentPredicate predicate, Keyword keyword) {
        this(predicate, keyword, List.of());
    }

    public GrantKeywordToTriggeringAttackersEffect {
        attackingCreatures = List.copyOf(attackingCreatures);
    }

    @Override
    public CardEffect withAttackingCreatures(List<AttackingPermanentSnapshot> attackers) {
        return new GrantKeywordToTriggeringAttackersEffect(attackingCreaturePredicate, keyword, attackers);
    }
}
