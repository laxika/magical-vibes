package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static restriction: the activated abilities with {T} in their costs of the permanents {@code subject} names can't be
 * activated (CR 602.5); their other activated abilities stay usable. Contrast
 * {@link ActivatedAbilitiesOfMatchingPermanentsCantBeActivatedEffect}, which locks every activated ability. Enforced in
 * {@code AbilityActivationService} on the mana-tap and {@code isRequiresTap()} paths, and in
 * {@code GameQueryService.canActivateManaAbility}, all through {@code GameQueryService.isLockedFromActivatingTapAbilities}.
 *
 * <p>Forms: a set, {@code controlledBy(eachPlayer(), filter)} ("creatures with flying … their activated abilities with
 * {T} in their costs can't be activated", Katabatic Winds; the {@code (PermanentPredicate)} constructor), judged as
 * an active static ability of the source; {@code attachedCreature()} ("enchanted creature's activated abilities with
 * {T} in their costs can't be activated", Serra Bestiary), an Aura's ability read like the Aura's other effects on its
 * creature.
 *
 * @param subject which permanents have their {T} abilities locked
 */
public record MatchingPermanentsCantActivateTapAbilitiesEffect(PermanentSubject subject) implements CardEffect {

    /** Every permanent matching {@code affectedPredicate}, any player's. */
    public MatchingPermanentsCantActivateTapAbilitiesEffect(PermanentPredicate affectedPredicate) {
        this(Subjects.controlledBy(Subjects.eachPlayer(), affectedPredicate));
    }

    public MatchingPermanentsCantActivateTapAbilitiesEffect {
        if (!(subject instanceof PermanentSubject.ControlledBy || subject instanceof PermanentSubject.Attached)) {
            throw new IllegalArgumentException("Unsupported tap-ability lock subject: " + subject);
        }
    }

    /** Whether this is an Aura's or Equipment's lock on the permanent it is attached to. */
    public boolean locksAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }
}
