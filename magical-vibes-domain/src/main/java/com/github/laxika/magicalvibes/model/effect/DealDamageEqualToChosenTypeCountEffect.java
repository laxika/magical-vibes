package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type. This deals damage to {target} equal to the number of permanents you
 * control of the chosen type." — any target (Roar of the Crowd) or target creature
 * (Coordinated Barrage).
 *
 * <p>On resolution the controller is prompted to choose a creature type (stored on
 * {@code GameData.chosenSpellSubtype}); the effect then re-runs, counts the controller's
 * permanents of that type (Changeling-aware), and deals that much damage to the target.</p>
 *
 * @param declaredTarget what the card names as its target — {@link TargetPredicates#anyTarget()}
 *                       or {@link TargetPredicates#creature()}. Nothing else is printed on this
 *                       shape, and the two route to different damage paths, so the constructor
 *                       rejects any other declaration rather than silently damaging nothing
 */
public record DealDamageEqualToChosenTypeCountEffect(TargetPredicate declaredTarget) implements CardEffect {

    public DealDamageEqualToChosenTypeCountEffect {
        if (!TargetPredicates.anyTarget().equals(declaredTarget)
                && !TargetPredicates.creature().equals(declaredTarget)) {
            throw new IllegalArgumentException(
                    "declaredTarget must be TargetPredicates.anyTarget() or TargetPredicates.creature()");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(declaredTarget);
    }
}
