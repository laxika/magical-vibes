package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * STATIC "[these creatures] block each combat if able": whenever its controller is the defending player, each creature
 * {@code subject} names must be declared as a blocker of some attacker it can legally block.
 *
 * <p>{@link Subjects#source()} (the default) is "this creature blocks each combat if able" (Watchdog): a requirement read
 * off the creature's own static effects every combat. Unlike {@link SetCombatRequirementThisTurnEffect} with
 * {@link CombatRequirement#MUST_BLOCK} (a one-shot that puts this effect on the creature as a floating effect until end
 * of turn). A set such as {@link Subjects#allCreatures()} is "all creatures block each combat if able" (Grand Melee,
 * Invasion Plans): the source's own ability, read from it, so a creature that lost its abilities is still bound by it.
 * All are enforced in {@code CombatBlockService.validateMustBlockIfAbleRequirements}; satisfied vacuously when the
 * creature can't legally block any declared attacker.
 *
 * @param subject the creatures that must block: {@code source()} or a static set
 */
public record MustBlockEachCombatEffect(PermanentSubject subject) implements CardEffect {

    public MustBlockEachCombatEffect {
        Objects.requireNonNull(subject, "subject");
        if (!(subject instanceof PermanentSubject.Source) && (!subject.isSet() || subject.declaresTarget())) {
            throw new IllegalArgumentException("This creature or a static set of creatures blocks: " + subject);
        }
    }

    public MustBlockEachCombatEffect() {
        this(Subjects.source());
    }
}
