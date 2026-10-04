package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaultRiders.class, RhysticCave.class})
class FaultRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a land gives +2/+0 and first strike")
    void sacrificeLandBoostsAndGrantsFirstStrike() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rhystic Cave");
        assertThat(riders.getEffectivePower()).isEqualTo(4);
        assertThat(riders.getEffectiveToughness()).isEqualTo(2);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void effectsWearOff() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(riders.getEffectivePower()).isEqualTo(2);
        assertThat(riders.getEffectiveToughness()).isEqualTo(2);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOncePerTurn() {
        harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Once-per-turn restriction resets on the next turn")
    void oncePerTurnRestrictionResetsOnNextTurn() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new RhysticCave());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(riders.getEffectivePower()).isEqualTo(4);
        assertThat(riders.getEffectiveToughness()).isEqualTo(2);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot be activated without a land to sacrifice")
    void requiresLand() {
        harness.addToBattlefieldAndReturn(player1, new FaultRiders());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Land is sacrificed as a cost before the boost resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Rhystic Cave");
        harness.assertNotOnBattlefield(player1, "Rhystic Cave");
        assertThat(riders.getEffectivePower()).isEqualTo(2);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new RhysticCave());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Rhystic Cave");

        harness.passBothPriorities();

        assertThat(riders.getEffectivePower()).isEqualTo(4);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsLand() {
        harness.addToBattlefield(player1, new FaultRiders());
        harness.addToBattlefield(player2, new RhysticCave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Rhystic Cave");
        harness.assertNotInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("A failed attempt does not consume the activation for the turn")
    void failedAttemptDoesNotConsumeActivation() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new FaultRiders());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new RhysticCave());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(riders.getEffectivePower()).isEqualTo(4);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Rhystic Cave");
    }

    @Test
    @DisplayName("Tapped Fault Riders can sacrifice a tapped land")
    void tappedPermanentsDoNotPreventActivation() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        riders.tap();
        land.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(riders.isTapped()).isTrue();
        assertThat(riders.getEffectivePower()).isEqualTo(4);
        assertThat(riders.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Rhystic Cave");
    }

    @Test
    @DisplayName("Each Fault Riders has an independent activation limit")
    void separateCopiesCanEachActivate() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FaultRiders());
        harness.addToBattlefield(player1, new RhysticCave());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(2);
        assertThat(second.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new RhysticCave());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(first.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(second.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }
}
