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
        harness.passUntil(player2, TurnStep.UNTAP);

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
}
