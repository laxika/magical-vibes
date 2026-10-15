package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClockOfOmens.class, Arachnoid.class, DrossCrocodile.class})
class ClockOfOmensTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping two untapped artifacts untaps target artifact")
    void untapsTargetArtifact() {
        Permanent clock = addClock(player1);
        Permanent cost1 = addArtifact(player1, false);
        Permanent cost2 = addArtifact(player1, false);
        Permanent target = addArtifact(player1, true);

        activateClock(clock, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(cost1.isTapped()).isTrue();
        assertThat(cost2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Clock itself can be tapped to pay its cost")
    void canTapSourceAsCost() {
        Permanent clock = addClock(player1);
        clock.untap();
        Permanent otherArtifact = addArtifact(player1, false);
        Permanent target = addArtifact(player1, true);

        activateClock(clock, target.getId());
        harness.passBothPriorities();

        assertThat(clock.isTapped()).isTrue();
        assertThat(otherArtifact.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap an artifact an opponent controls")
    void untapsOpponentArtifact() {
        Permanent clock = addClock(player1);
        addArtifact(player1, false);
        addArtifact(player1, false);
        Permanent target = addArtifact(player2, true);

        activateClock(clock, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without two untapped artifacts")
    void cannotActivateWithoutTwoUntappedArtifacts() {
        Permanent clock = addClock(player1);
        Permanent other = addArtifact(player1, false);
        Permanent target = addArtifact(player1, true);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(clock);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(other.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent clock = addClock(player1);
        addArtifact(player1, false);
        addArtifact(player1, false);

        Permanent creature = addCreatureReady(player1, new DrossCrocodile());
        creature.tap();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(clock);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Summoning-sick artifact creatures can pay the tap cost")
    void canTapSummoningSickArtifacts() {
        Permanent clock = addClock(player1);
        Permanent cost1 = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Permanent cost2 = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        cost1.setSummoningSick(true);
        cost2.setSummoningSick(true);
        Permanent target = addArtifact(player1, true);

        activateClock(clock, target.getId());

        assertThat(cost1.isTapped()).isTrue();
        assertThat(cost2.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped target can also be tapped to pay the cost")
    void canTapTargetAsCost() {
        Permanent clock = addClock(player1);
        Permanent target = addArtifact(player1, false);
        Permanent other = addArtifact(player1, false);

        activateClock(clock, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Clock can target itself while tapped")
    void canUntapSource() {
        Permanent clock = addClock(player1);
        Permanent cost1 = addArtifact(player1, false);
        Permanent cost2 = addArtifact(player1, false);

        activateClock(clock, clock.getId());
        harness.passBothPriorities();

        assertThat(clock.isTapped()).isFalse();
        assertThat(cost1.isTapped()).isTrue();
        assertThat(cost2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent artifacts and non-artifacts cannot supply the missing cost")
    void cannotUseOpponentArtifactsOrNonArtifactsForCost() {
        Permanent clock = addClock(player1);
        Permanent ownArtifact = addArtifact(player1, false);
        Permanent opponentArtifact = addArtifact(player2, false);
        Permanent creature = addCreatureReady(player1, new DrossCrocodile());

        assertThatThrownBy(() -> activateClock(clock, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ownArtifact.isTapped()).isFalse();
        assertThat(opponentArtifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller chooses two artifacts when more than two are available")
    void choosesWhichArtifactsToTap() {
        Permanent clock = addClock(player1);
        Permanent unchosen = addArtifact(player1, false);
        Permanent cost1 = addArtifact(player1, false);
        Permanent cost2 = addArtifact(player1, false);
        Permanent target = addArtifact(player1, true);

        activateClock(clock, target.getId());
        harness.handlePermanentChosen(player1, cost1.getId());
        harness.handlePermanentChosen(player1, cost2.getId());
        harness.passBothPriorities();

        assertThat(unchosen.isTapped()).isFalse();
        assertThat(cost1.isTapped()).isTrue();
        assertThat(cost2.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    /**
     * Activates the Clock. With exactly two untapped artifacts on the battlefield the cost
     * auto-selects them, so no permanent choice needs to be answered.
     */
    private void activateClock(Permanent clock, UUID targetId) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(clock);
        harness.activateAbility(player1, idx, null, targetId);
    }

    private Permanent addArtifact(Player player, boolean tapped) {
        Permanent permanent = addCreatureReady(player, new Arachnoid());
        if (tapped) {
            permanent.tap();
        }
        return permanent;
    }

    private Permanent addClock(Player player) {
        Permanent clock = harness.addToBattlefieldAndReturn(player, new ClockOfOmens());
        clock.setSummoningSick(false);
        clock.tap();
        return clock;
    }
}
