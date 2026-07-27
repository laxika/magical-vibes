package com.github.laxika.magicalvibes.model.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins what each factory builds. The predicate decides which targets are legal and the
 * message is shown to the player who picked an illegal one, so both are behaviour: a card
 * that switched to a factory must get exactly the filter it spelled out before.
 */
class TargetFiltersTest {

    @Test
    @DisplayName("creature() restricts to creatures")
    void creature() {
        assertThat(TargetFilters.creature()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature"));
    }

    @Test
    @DisplayName("creatureYouControl() restricts to creatures the source's controller controls")
    void creatureYouControl() {
        assertThat(TargetFilters.creatureYouControl()).isEqualTo(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature you control"));
    }

    @Test
    @DisplayName("creatureAnOpponentControls() is a creature the controller does not control")
    void creatureAnOpponentControls() {
        assertThat(TargetFilters.creatureAnOpponentControls()).isEqualTo(
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(
                                        new PermanentControlledBySourceControllerPredicate()))),
                        "Target must be a creature an opponent controls"));
    }

    @Test
    @DisplayName("attackingCreature() restricts to attacking creatures")
    void attackingCreature() {
        assertThat(TargetFilters.attackingCreature()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsAttackingPredicate(), "Target must be an attacking creature"));
    }

    @Test
    @DisplayName("land() and landYouControl() restrict to lands")
    void lands() {
        assertThat(TargetFilters.land()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsLandPredicate(), "Target must be a land"));
        assertThat(TargetFilters.landYouControl()).isEqualTo(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(), "Target must be a land you control"));
    }

    @Test
    @DisplayName("artifact() and enchantment() restrict to their card type")
    void artifactAndEnchantment() {
        assertThat(TargetFilters.artifact()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Target must be an artifact"));
        assertThat(TargetFilters.enchantment()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsEnchantmentPredicate(), "Target must be an enchantment"));
    }

    @Test
    @DisplayName("permanent() accepts any permanent; permanentYouControl() narrows to yours")
    void permanents() {
        assertThat(TargetFilters.permanent()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(), "Target must be a permanent"));
        assertThat(TargetFilters.permanentYouControl()).isEqualTo(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(), "Target must be a permanent you control"));
    }
}
