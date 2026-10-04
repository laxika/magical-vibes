package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AetherChaser;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FenHauler.class, Ornithopter.class, AetherChaser.class})
class FenHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent hauler = harness.addToBattlefieldAndReturn(player1, new FenHauler());
        hauler.setSummoningSick(false);
        hauler.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        blocker.setSummoningSick(false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a non-artifact creature")
    void canBeBlockedByNonArtifactCreature() {
        Permanent hauler = harness.addToBattlefieldAndReturn(player1, new FenHauler());
        hauler.setSummoningSick(false);
        hauler.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new AetherChaser());
        blocker.setSummoningSick(false);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void improvisePaysGenericManaWithSummoningSickArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(true);
        harness.setHand(player1, List.of(new FenHauler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fen Hauler");
    }

    @Test
    void improviseCannotPayBlackMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FenHauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Fen Hauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsTappedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new FenHauler()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Fen Hauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseRejectsNonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AetherChaser());
        harness.setHand(player1, List.of(new FenHauler()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Fen Hauler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void improviseCannotTapSameArtifactTwice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new FenHauler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        harness.assertInHand(player1, "Fen Hauler");
        assertThat(gd.stack).isEmpty();
    }
}
