package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThornhideWolves;
import com.github.laxika.magicalvibes.cards.w.WatcherInTheWeb;
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

@CardUsed({StormriderSpirit.class, ThornhideWolves.class, WatcherInTheWeb.class})
class StormriderSpiritTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new StormriderSpirit()));
        addCastingMana();

        harness.passPriority(player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Stormrider Spirit");
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new StormriderSpirit());
        addCreatureReady(player2, new ThornhideWolves());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting in response to another creature spell")
    void canCastInResponseToCreatureSpell() {
        harness.setHand(player1, List.of(new StormriderSpirit()));
        harness.setHand(player2, List.of(new StormriderSpirit()));
        addCastingMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player2, "Stormrider Spirit")).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Stormrider Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("A flying creature can block Stormrider Spirit")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new StormriderSpirit());
        Permanent blocker = addCreatureReady(player2, new StormriderSpirit());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("A creature with reach can block Stormrider Spirit")
    void creatureWithReachCanBlock() {
        addCreatureReady(player1, new StormriderSpirit());
        Permanent blocker = addCreatureReady(player2, new WatcherInTheWeb());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }
}
