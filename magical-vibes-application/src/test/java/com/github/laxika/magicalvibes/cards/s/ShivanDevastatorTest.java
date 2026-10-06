package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShivanDevastator.class, YavimayaIconoclast.class})
class ShivanDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new ShivanDevastator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent devastator = findPermanent(player1, "Shivan Devastator");
        assertThat(devastator).isNotNull();
        assertThat(devastator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 puts a 0/0 Shivan Devastator into the graveyard")
    void entersWithZeroCountersAndDies() {
        harness.setHand(player1, List.of(new ShivanDevastator()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shivan Devastator");
    }

    @Test
    @DisplayName("Haste allows attacking on the turn it is cast, dealing damage from its counters")
    void attacksOnTheTurnItIsCast() {
        harness.setHand(player1, List.of(new ShivanDevastator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void groundCreatureCannotBlock() {
        harness.addToBattlefield(player2, new YavimayaIconoclast());
        harness.setHand(player1, List.of(new ShivanDevastator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Entering without being cast gives no counters and the creature dies")
    void enteringWithoutBeingCastUsesZeroForX() {
        Permanent devastator = harness.enterBattlefieldAndReturn(player1, new ShivanDevastator());
        harness.runStateBasedActions();

        assertThat(devastator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Shivan Devastator");
        harness.assertInGraveyard(player1, "Shivan Devastator");
    }
}
