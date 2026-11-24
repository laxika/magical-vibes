package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Until end of turn, this permanent loses '[static ability]'" (Glittering Lion), "target creature loses all '[static
 * ability]' abilities until end of turn" (Tolaria, Shelkin Brownie): the permanents {@code subject} names lose their
 * static abilities of {@code effectType} until end of turn, a layer-6 ability removal (CR 613.1f). As it resolves it
 * becomes a floating effect on each of them with this record as its payload; the abilities of that kind they have
 * then are removed, one granted later stays (CR 613.7).
 *
 * @param effectType the concrete static effect type to suppress
 * @param subject    who loses it: {@code source()} or {@code target()} (a benign creature target)
 */
public record SuppressStaticEffectUntilEndOfTurnEffect(
        Class<? extends CardEffect> effectType,
        PermanentSubject subject
) implements CardEffect {

    public SuppressStaticEffectUntilEndOfTurnEffect {
        Objects.requireNonNull(effectType, "effectType");
        Objects.requireNonNull(subject, "subject");
    }

    /** "Until end of turn, this permanent loses '[static ability]'." */
    public SuppressStaticEffectUntilEndOfTurnEffect(Class<? extends CardEffect> effectType) {
        this(effectType, Subjects.source());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
