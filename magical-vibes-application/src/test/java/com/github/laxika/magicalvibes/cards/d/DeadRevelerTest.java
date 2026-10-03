package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadReveler.class, GarruksPackleader.class})
class DeadRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting unleash puts a +1/+1 counter on it as it enters")
    void unleashedEntersWithCounter() {
        castDeadReveler(true);

        Permanent reveler = findPermanent(player1, "Dead Reveler");
        assertThat(reveler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, reveler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, reveler)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining unleash leaves it without a counter")
    void decliningLeavesNoCounter() {
        castDeadReveler(false);

        Permanent reveler = findPermanent(player1, "Dead Reveler");
        assertThat(reveler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unleashed Dead Reveler can't block")
    void unleashedCantBlock() {
        Permanent reveler = addCreatureReady(player1, new DeadReveler());
        reveler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new DeadReveler());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without a +1/+1 counter it blocks normally")
    void blocksWithoutCounter() {
        addCreatureReady(player1, new DeadReveler());
        addCreatureReady(player2, new DeadReveler());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Dead Reveler").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The restriction is block-only — an unleashed Dead Reveler can still attack")
    void unleashedCanStillAttack() {
        harness.setLife(player2, 20);
        Permanent reveler = addCreatureReady(player1, new DeadReveler());
        reveler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An unleashed Dead Reveler can block after its last +1/+1 counter is removed")
    void blocksAfterUnleashCounterIsRemoved() {
        castDeadReveler(true);
        Permanent reveler = findPermanent(player1, "Dead Reveler");
        reveler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addCreatureReady(player2, new DeadReveler());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(reveler.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters do not prevent blocking")
    void blocksWithAnotherCounterType() {
        Permanent reveler = addCreatureReady(player1, new DeadReveler());
        reveler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreatureReady(player2, new DeadReveler());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(reveler.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The unleash counter counts toward power when checking enters triggers")
    void unleashedEntryTriggersPackleader() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new DeadReveler()));

        castDeadReveler(true);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining unleash does not meet Packleader's entering power threshold")
    void decliningUnleashDoesNotTriggerPackleader() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new DeadReveler()));

        castDeadReveler(false);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castDeadReveler(boolean unleash) {
        harness.setHand(player1, List.of(new DeadReveler()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
    }
}
