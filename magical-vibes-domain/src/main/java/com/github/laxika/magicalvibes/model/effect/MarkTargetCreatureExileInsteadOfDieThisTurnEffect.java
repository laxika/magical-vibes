package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks the target creature so that if it would die this turn, it is exiled instead: a replacement effect
 * (CR 614.1a) the resolution makes as a floating effect on that creature until end of turn
 * ({@code GameData.exileInsteadOfDying}); the damage effects with an "exile it instead" rider make the same one.
 * Place before a damage effect on the same target so lethal damage triggers the replacement.
 * When {@code trackWithSource} is true, the replacement exiles the creature card with the
 * source permanent. Used by Wilt in the Heat and Gut, Fanatical Priestess.
 *
 * <p>With {@link Subjects#thisWay} the subject is "a permanent dealt damage this way" (Spikefield Hazard): every
 * permanent it names is marked, a planeswalker too, and nothing is targeted.
 *
 * @param subject         {@link Subjects#target()} (target creature) or a {@link PermanentSubject.ThisWay} step result
 * @param trackWithSource whether the exiled card is linked to the source permanent
 */
public record MarkTargetCreatureExileInsteadOfDieThisTurnEffect(PermanentSubject subject, boolean trackWithSource)
        implements CardEffect {

    public MarkTargetCreatureExileInsteadOfDieThisTurnEffect {
        subject = subject == null ? Subjects.target() : subject;
        if (!(subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.ThisWay)) {
            throw new IllegalArgumentException("Unsupported exile-instead subject: " + subject);
        }
    }

    public MarkTargetCreatureExileInsteadOfDieThisTurnEffect(boolean trackWithSource) {
        this(Subjects.target(), trackWithSource);
    }

    public MarkTargetCreatureExileInsteadOfDieThisTurnEffect() {
        this(false);
    }

    /** Marks the permanents {@code subject} names, with no link to the source. */
    public MarkTargetCreatureExileInsteadOfDieThisTurnEffect(PermanentSubject subject) {
        this(subject, false);
    }

    public static MarkTargetCreatureExileInsteadOfDieThisTurnEffect withSourceTracking() {
        return new MarkTargetCreatureExileInsteadOfDieThisTurnEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.harmful(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
