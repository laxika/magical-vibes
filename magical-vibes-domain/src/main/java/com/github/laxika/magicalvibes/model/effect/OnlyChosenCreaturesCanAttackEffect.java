package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * "The chosen creatures attack if able, and other creatures can't attack" (Oracle en-Vec), as a floating effect that
 * applies during the chosen player's next turn: every creature outside {@code chosen} can't attack (CR 508.1c), and
 * each creature inside it attacks if able (CR 508.1d). An empty choice bars every creature.
 *
 * @param chosen the permanents the player chose
 */
public record OnlyChosenCreaturesCanAttackEffect(Set<java.util.UUID> chosen) implements CombatAttackRequirementEffect {

    public OnlyChosenCreaturesCanAttackEffect {
        chosen = Set.copyOf(chosen);
    }

    @Override
    public PermanentPredicate affectedPredicate() {
        return new PermanentAnyOfPredicate(chosen.stream()
                .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
    }
}
