package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemurCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 boosts your creature before it fights an opposing creature")
    void boostsAndFights() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMode(0, List.of(own.getId(), opposing.getId()));

        assertThat(own.getEffectivePower()).isEqualTo(3);
        assertThat(own.getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mode 1 counters a spell whose controller cannot pay {3}")
    void countersSpellWithoutPayment() {
        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 1, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Mode 1 cannot target a permanent")
    void counterModeRejectsPermanentTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 prevents creatures with power 3 or less from blocking")
    void preventsLowPowerBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent lowPowerBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent highPowerBlocker = addCreatureReady(player2, new AirElemental());

        castMode(2, List.of());

        assertThat(lowPowerBlocker.isCantBlockThisTurn()).isTrue();
        assertThat(highPowerBlocker.isCantBlockThisTurn()).isFalse();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMode(int modeIndex, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new TemurCharm()));
        addTemurMana(player1);
        harness.castModalInstant(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }

    private void addTemurMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
