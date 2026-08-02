package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Tap target [permanent]" resolved as a controller choice at resolution rather than a cast-time
 * target. The tapping counterpart of {@link UntapChosenPermanentEffect}, for triggered abilities on
 * slots with no targeting pipeline (e.g. {@code ON_DAMAGE_TO_PLAYER}): the effect is pushed as a
 * non-targeting stack entry and, at resolution, the controller chooses one permanent matching
 * {@code predicate} across every battlefield to tap.
 *
 * <p>With {@code preventUntapWhileSourceTapped} the chosen permanent additionally doesn't untap
 * during its controller's untap step for as long as the source permanent remains tapped — the same
 * lock {@link DoesntUntapEffect#targetWhileSourceTapped()} records, but bound to the chosen
 * permanent instead of a cast-time target (Thalakos Dreamsower).
 *
 * @param predicate                     filter restricting the choosable permanents
 * @param preventUntapWhileSourceTapped whether the chosen permanent is also untap-locked while the
 *                                      source stays tapped
 */
public record TapChosenPermanentEffect(PermanentPredicate predicate,
                                       boolean preventUntapWhileSourceTapped)
        implements CombatDamageTriggerContextEffect {

    /** The untap lock is keyed to the damage-dealing permanent, so the trigger binds it as its source. */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
