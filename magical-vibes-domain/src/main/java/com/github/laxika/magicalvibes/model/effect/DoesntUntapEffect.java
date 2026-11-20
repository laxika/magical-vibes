package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a permanent so it doesn't untap during its controller's untap step. The {@link PermanentSubject}
 * names the permanent and the {@link UntapLockCondition} selects how long the prevention lasts. Subject and
 * condition are tightly coupled, so instances are built through the static factories below rather than a raw
 * constructor:
 *
 * <ul>
 *   <li>{@link #self()} — {@code source()}+{@code ALWAYS}: the source permanent never untaps (a
 *       continuous static effect read during the untap step; Colossus of Sardia, Traxos, Grimgrin,
 *       Elaborate Firecannon). Added as a static ability.</li>
 *   <li>{@link #enchanted()} — {@code attached()}+{@code ALWAYS}: the permanent the source aura or
 *       equipment is attached to never untaps (Claustrophobia, Dehydration, Numbing Dose, Heavy
 *       Arbalest). Added as a static ability on the aura and read via aura lookup during
 *       the untap step.</li>
 *   <li>{@link #targetWhileSourceOnBattlefield()} — {@code target()}+{@code WHILE_SOURCE_ON_BATTLEFIELD}:
 *       the chosen target doesn't untap while the source stays on the battlefield (Dungeon Geists,
 *       Time of Ice). Resolved on the stack by {@code DoesntUntapEffectHandler}.</li>
 *   <li>{@link #triggeringWhileSourceOnBattlefield()} — {@code triggering()}+{@code WHILE_SOURCE_ON_BATTLEFIELD}:
 *       the stack entry's triggering permanent doesn't untap while the source stays on the battlefield (Neko-Te's
 *       "that creature doesn't untap … for as long as this Equipment remains on the battlefield").</li>
 *   <li>{@link #targetWhileSourceControlled()} — {@code target()}+{@code WHILE_SOURCE_CONTROLLED}:
 *       the chosen target doesn't untap while the ability's controller controls its source
 *       (Wall of Stolen Identity).</li>
 *   <li>{@link #targetWhileSourceTapped()} — {@code target()}+{@code WHILE_SOURCE_TAPPED}: the chosen
 *       target doesn't untap while the source stays tapped (Rust Tick). Resolved on the stack.</li>
 * </ul>
 *
 * <p>The {@code target()} variants declare no targeting flags — they piggyback on the targeting from a
 * companion {@link TapPermanentsEffect} (or a saga-chapter/ability target filter) on the same slot.
 *
 * <p>Replaces the former {@code DoesntUntapDuringUntapStepEffect} (SELF/ALWAYS),
 * {@code AttachedCreatureDoesntUntapEffect} (ENCHANTED/ALWAYS),
 * {@code PreventTargetUntapWhileSourceOnBattlefieldEffect} (TARGET/WHILE_SOURCE_ON_BATTLEFIELD) and
 * {@code PreventTargetUntapWhileSourceTappedEffect} (TARGET/WHILE_SOURCE_TAPPED).
 *
 * @param subject   which permanent's untap is prevented
 * @param condition how long the prevention lasts
 */
public record DoesntUntapEffect(PermanentSubject subject, UntapLockCondition condition) implements PermanentLockEffect {

    @Override
    public boolean endsWhenSourceControllerChanges() {
        return condition == UntapLockCondition.WHILE_SOURCE_CONTROLLED;
    }

    /** Target doesn't untap for as long as the resolving ability's controller controls its source. */
    public static DoesntUntapEffect targetWhileSourceControlled() {
        return new DoesntUntapEffect(Subjects.target(), UntapLockCondition.WHILE_SOURCE_CONTROLLED);
    }

    public DoesntUntapEffect {
        boolean staticSubject = subject instanceof PermanentSubject.Source
                || subject instanceof PermanentSubject.Attached;
        if (condition == UntapLockCondition.ALWAYS) {
            if (!staticSubject) {
                throw new IllegalArgumentException("ALWAYS requires a source() or attached() subject, got " + subject);
            }
        } else if (!(subject instanceof PermanentSubject.Target)
                && !(subject instanceof PermanentSubject.Triggering
                && condition == UntapLockCondition.WHILE_SOURCE_ON_BATTLEFIELD)) {
            throw new IllegalArgumentException(condition + " requires a target() subject, got " + subject);
        }
    }

    /** {@code source()}+{@code ALWAYS}: the source permanent never untaps (static). */
    public static DoesntUntapEffect self() {
        return new DoesntUntapEffect(Subjects.source(), UntapLockCondition.ALWAYS);
    }

    /** {@code attached()}+{@code ALWAYS}: the attached aura/equipment's host never untaps (static). */
    public static DoesntUntapEffect enchanted() {
        return new DoesntUntapEffect(Subjects.attached(), UntapLockCondition.ALWAYS);
    }

    /** {@code target()}+{@code WHILE_SOURCE_ON_BATTLEFIELD}: target doesn't untap while the source stays on the battlefield. */
    public static DoesntUntapEffect targetWhileSourceOnBattlefield() {
        return new DoesntUntapEffect(Subjects.target(), UntapLockCondition.WHILE_SOURCE_ON_BATTLEFIELD);
    }

    /** {@code triggering()}+{@code WHILE_SOURCE_ON_BATTLEFIELD}: that permanent doesn't untap while the source stays. */
    public static DoesntUntapEffect triggeringWhileSourceOnBattlefield() {
        return new DoesntUntapEffect(Subjects.triggering(), UntapLockCondition.WHILE_SOURCE_ON_BATTLEFIELD);
    }

    /** {@code target()}+{@code WHILE_SOURCE_TAPPED}: target doesn't untap while the source stays tapped. */
    public static DoesntUntapEffect targetWhileSourceTapped() {
        return new DoesntUntapEffect(Subjects.target(), UntapLockCondition.WHILE_SOURCE_TAPPED);
    }
}
