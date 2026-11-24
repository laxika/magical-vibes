package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;

import java.util.Objects;

/**
 * The controller looks at the card each face-down creature {@code creatures} names represents: "look at target
 * face-down creature" ({@code target()}, the default), "look at each face-down creature that's attacking or blocking"
 * (Revealing Wind), "any face-down creatures [target player] controls" (Spy Network). The look is private to the
 * controller (CR 701.20e) and the log names no card; a permanent that isn't a face-down creature as the effect
 * resolves is skipped. Only {@code target()} declares a (benign) target, of a face-down creature.
 *
 * @param creatures which face-down creatures are looked at
 */
public record LookAtFaceDownCreatureEffect(PermanentSubject creatures) implements CardEffect {

    public LookAtFaceDownCreatureEffect {
        Objects.requireNonNull(creatures, "creatures");
    }

    /** "Look at target face-down creature." */
    public LookAtFaceDownCreatureEffect() {
        this(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return creatures.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.creature(), new PermanentIsFaceDownPredicate())
                : TargetSpec.NONE;
    }
}
