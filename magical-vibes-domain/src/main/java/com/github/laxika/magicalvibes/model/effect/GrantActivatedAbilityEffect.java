package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

public record GrantActivatedAbilityEffect(
        ActivatedAbility ability,
        PermanentSubject subject,
        PermanentPredicate filter,
        Duration duration,
        UUID expirationCardId
) implements CardEffect {

    /** Continuous (static) grant — existing behavior. */
    public GrantActivatedAbilityEffect(ActivatedAbility ability, PermanentSubject subject) {
        this(ability, subject, null, Duration.STATIC, null);
    }

    /** Continuous (static) grant with filter — existing behavior. */
    public GrantActivatedAbilityEffect(ActivatedAbility ability, PermanentSubject subject, PermanentPredicate filter) {
        this(ability, subject, filter, Duration.STATIC, null);
    }

    public GrantActivatedAbilityEffect(ActivatedAbility ability, PermanentSubject subject,
                                       PermanentPredicate filter, Duration duration) {
        this(ability, subject, filter, duration, null);
    }

    /** TARGET subject grants the ability to the spell/ability's target permanent (e.g. Banishing Knack). */
    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }

    /**
     * The ENCHANTED_PERMANENT subject grants the ability to the Aura's attached permanent, so an
     * activation whose source is that Aura captures the attached permanent at activation time.
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return Subjects.attached().equals(subject);
    }
}
