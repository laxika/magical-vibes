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
 * <p>With {@code skipNextUntap}, the chosen permanent skips its controller's next untap step.
 *
 * @param predicate                     filter restricting the choosable permanents
 * @param preventUntapWhileSourceTapped whether the chosen permanent is also untap-locked while the
 *                                      source stays tapped
 * @param preventUntapWhileSourceOnBattlefield whether the chosen permanent is also untap-locked
 *                                              while the source stays on the battlefield
 * @param chooseFromDamagedPlayer        whether the choice is restricted to the damaged player's
 *                                      battlefield
 * @param skipNextUntap                  whether the chosen permanent skips its controller's next
 *                                      untap step
 */
public record TapChosenPermanentEffect(PermanentPredicate predicate,
                                       boolean preventUntapWhileSourceTapped,
                                       boolean preventUntapWhileSourceOnBattlefield,
                                       boolean chooseFromDamagedPlayer,
                                       boolean skipNextUntap)
        implements CombatDamageTriggerContextEffect {

    public TapChosenPermanentEffect(PermanentPredicate predicate, boolean preventUntapWhileSourceTapped) {
        this(predicate, preventUntapWhileSourceTapped, false, false, false);
    }

    public static TapChosenPermanentEffect damagedPlayerControls(PermanentPredicate predicate) {
        return new TapChosenPermanentEffect(predicate, false, true, true, false);
    }

    public static TapChosenPermanentEffect damagedPlayerControlsAndSkipsNextUntap(PermanentPredicate predicate) {
        return new TapChosenPermanentEffect(predicate, false, false, true, true);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return chooseFromDamagedPlayer ? TriggerContext.DAMAGED_PLAYER : TriggerContext.SOURCE_SELF;
    }
}
