package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a permanent so it doesn't untap during its controller's untap step. The
 * {@link TapUntapScope} selects which permanent is affected and the {@link UntapLockCondition}
 * selects how long the prevention lasts. Scope and condition are tightly coupled, so instances are
 * built through the four static factories below rather than a raw constructor:
 *
 * <ul>
 *   <li>{@link #self()} — {@code SELF}+{@code ALWAYS}: the source permanent never untaps (a
 *       continuous static effect read during the untap step; Colossus of Sardia, Traxos, Grimgrin,
 *       Elaborate Firecannon). Placed in {@code EffectSlot.STATIC}.</li>
 *   <li>{@link #enchanted()} — {@code ENCHANTED}+{@code ALWAYS}: the permanent the source aura or
 *       equipment is attached to never untaps (Claustrophobia, Dehydration, Numbing Dose, Heavy
 *       Arbalest). Placed in {@code EffectSlot.STATIC} on the aura and read via aura lookup during
 *       the untap step.</li>
 *   <li>{@link #targetWhileSourceOnBattlefield()} — {@code TARGET}+{@code WHILE_SOURCE_ON_BATTLEFIELD}:
 *       the chosen target doesn't untap while the source stays on the battlefield (Dungeon Geists,
 *       Time of Ice). Resolved on the stack by {@code DoesntUntapEffectHandler}.</li>
 *   <li>{@link #targetWhileSourceTapped()} — {@code TARGET}+{@code WHILE_SOURCE_TAPPED}: the chosen
 *       target doesn't untap while the source stays tapped (Rust Tick). Resolved on the stack.</li>
 * </ul>
 *
 * <p>The {@code TARGET} variants declare no targeting flags — they piggyback on the targeting from a
 * companion {@link TapPermanentsEffect} (or a saga-chapter/ability target filter) on the same slot.
 *
 * <p>Replaces the former {@code DoesntUntapDuringUntapStepEffect} (SELF/ALWAYS),
 * {@code AttachedCreatureDoesntUntapEffect} (ENCHANTED/ALWAYS),
 * {@code PreventTargetUntapWhileSourceOnBattlefieldEffect} (TARGET/WHILE_SOURCE_ON_BATTLEFIELD) and
 * {@code PreventTargetUntapWhileSourceTappedEffect} (TARGET/WHILE_SOURCE_TAPPED).
 *
 * @param scope     which permanent's untap is prevented
 * @param condition how long the prevention lasts
 */
public record DoesntUntapEffect(TapUntapScope scope, UntapLockCondition condition) implements CardEffect {

    public DoesntUntapEffect {
        boolean staticScope = scope == TapUntapScope.SELF || scope == TapUntapScope.ENCHANTED;
        if (condition == UntapLockCondition.ALWAYS) {
            if (!staticScope) {
                throw new IllegalArgumentException("ALWAYS requires a SELF or ENCHANTED scope, got " + scope);
            }
        } else if (scope != TapUntapScope.TARGET) {
            throw new IllegalArgumentException(condition + " requires a TARGET scope, got " + scope);
        }
    }

    /** {@code SELF}+{@code ALWAYS}: the source permanent never untaps (static). */
    public static DoesntUntapEffect self() {
        return new DoesntUntapEffect(TapUntapScope.SELF, UntapLockCondition.ALWAYS);
    }

    /** {@code ENCHANTED}+{@code ALWAYS}: the attached aura/equipment's host never untaps (static). */
    public static DoesntUntapEffect enchanted() {
        return new DoesntUntapEffect(TapUntapScope.ENCHANTED, UntapLockCondition.ALWAYS);
    }

    /** {@code TARGET}+{@code WHILE_SOURCE_ON_BATTLEFIELD}: target doesn't untap while the source stays on the battlefield. */
    public static DoesntUntapEffect targetWhileSourceOnBattlefield() {
        return new DoesntUntapEffect(TapUntapScope.TARGET, UntapLockCondition.WHILE_SOURCE_ON_BATTLEFIELD);
    }

    /** {@code TARGET}+{@code WHILE_SOURCE_TAPPED}: target doesn't untap while the source stays tapped. */
    public static DoesntUntapEffect targetWhileSourceTapped() {
        return new DoesntUntapEffect(TapUntapScope.TARGET, UntapLockCondition.WHILE_SOURCE_TAPPED);
    }
}
