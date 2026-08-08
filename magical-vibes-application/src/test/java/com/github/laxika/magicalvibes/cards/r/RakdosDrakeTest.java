package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RakdosDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castDrake(true);

        Permanent drake = findPermanent(player1, "Rakdos Drake");
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castDrake(false);

        assertThat(findPermanent(player1, "Rakdos Drake").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Rakdos Drake can't block")
    void unleashedCantBlock() {
        Permanent drake = addCreatureReady(player1, new RakdosDrake());
        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new RakdosDrake());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Rakdos Drake").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction is block-only — an unleashed Rakdos Drake can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent drake = addCreatureReady(player1, new RakdosDrake());
        drake.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castDrake(boolean unleash) {
        harness.setHand(player1, List.of(new RakdosDrake()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
