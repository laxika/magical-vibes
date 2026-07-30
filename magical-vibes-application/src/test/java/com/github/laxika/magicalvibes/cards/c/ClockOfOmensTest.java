package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.tap();
        gd.playerBattlefields.get(player1.getId()).add(bears);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(clock);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
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
        Permanent permanent = new Permanent(new Ornithopter());
        permanent.setSummoningSick(false);
        if (tapped) {
            permanent.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addClock(Player player) {
        Permanent clock = new Permanent(new ClockOfOmens());
        clock.setSummoningSick(false);
        clock.tap();
        gd.playerBattlefields.get(player.getId()).add(clock);
        return clock;
    }
}
