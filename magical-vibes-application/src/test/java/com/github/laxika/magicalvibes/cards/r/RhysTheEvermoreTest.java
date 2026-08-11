package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RhysTheEvermoreTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants persist to another creature you control")
    void etbGrantsPersist() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isTrue();
    }

    @Test
    @DisplayName("Granted persist returns the creature with a -1/-1 counter")
    void grantedPersistReturnsCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist grant wears off at end of turn")
    void persistGrantWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castRhys(bears.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.PERSIST)).isFalse();
    }

    @Test
    @DisplayName("Activated ability removes the chosen number of counters across counter types")
    void removesChosenNumberOfCounters() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 1);
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice = (PendingInteraction.XValueChoice)
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                + bears.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability does not prompt when the target has no counters")
    void noPromptWithoutCounters() {
        Permanent rhys = addCreatureReady(player1, new RhysTheEvermore());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        prepareAbility();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Activated ability only targets creatures you control")
    void rejectsOpponentCreature() {
        addCreatureReady(player1, new RhysTheEvermore());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRhys(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RhysTheEvermore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
