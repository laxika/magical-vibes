package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RescuePepperPotts.class, Ornithopter.class, GrizzlyBears.class, Island.class, HowlingMine.class})
class RescuePepperPottsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an artifact you control and puts a +1/+1 counter on Rescue")
    void returnsArtifactAndGetsCounter() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castRescue(artifact.getId());

        harness.assertInHand(player1, "Ornithopter");
        Permanent rescue = findPermanent(player1, "Rescue, Pepper Potts");
        assertThat(rescue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a creature you control without putting a counter on Rescue")
    void returnsCreatureWithoutCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castRescue(creature.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent rescue = findPermanent(player1, "Rescue, Pepper Potts");
        assertThat(rescue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can choose no target")
    void canChooseNoTarget() {
        harness.castFromHand(player1, new RescuePepperPotts(), "{1}{U}");
        harness.passBothPriorities();

        Permanent rescue = findPermanent(player1, "Rescue, Pepper Potts");
        assertThat(rescue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can decline to return an artifact even when a legal target exists")
    void canDeclineLegalTarget() {
        harness.addToBattlefield(player1, new Ornithopter());

        harness.castFromHand(player1, new RescuePepperPotts(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
        assertThat(findPermanent(player1, "Rescue, Pepper Potts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Returning a noncreature artifact also puts a counter on Rescue")
    void returnsNoncreatureArtifactAndGetsCounter() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());

        castRescue(artifact.getId());

        harness.assertNotOnBattlefield(player1, "Howling Mine");
        harness.assertInHand(player1, "Howling Mine");
        assertThat(findPermanent(player1, "Rescue, Pepper Potts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact that leaves before resolution does not give Rescue a counter")
    void targetLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new RescuePepperPotts()));
        addRescueMana();
        harness.castCreature(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, artifact));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        assertThat(findPermanent(player1, "Rescue, Pepper Potts")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The artifact is still returned when Rescue leaves before resolution")
    void sourceLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new RescuePepperPotts()));
        addRescueMana();
        harness.castCreature(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();
        Permanent rescue = findPermanent(player1, "Rescue, Pepper Potts");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, rescue));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rescue, Pepper Potts");
        harness.assertInHand(player1, "Rescue, Pepper Potts");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInHand(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Can target only another artifact or creature you control")
    void rejectsIllegalTargets() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RescuePepperPotts()));
        addRescueMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another artifact or creature you control");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another artifact or creature you control");
    }

    private void castRescue(UUID targetId) {
        harness.setHand(player1, List.of(new RescuePepperPotts()));
        addRescueMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addRescueMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
