package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Give // Take is one card whose two halves (and their fusion) are the three modes of a single
 * modal sorcery, each paying its own total cost.
 */
class GiveTakeTest extends BaseCardTest {

    private static final int GIVE = 0;
    private static final int TAKE = 1;
    private static final int FUSE = 2;

    private void addGiveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addTakeMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Give puts three +1/+1 counters on the target creature")
    void givePutsThreeCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiveTake()));
        addGiveMana();

        harness.castSorcery(player1, 0, GIVE, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Give cannot target a player")
    void giveCannotTargetPlayer() {
        harness.setHand(player1, List.of(new GiveTake()));
        addGiveMana();

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, GIVE, playerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Take removes every +1/+1 counter and draws that many cards")
    void takeRemovesCountersAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new GiveTake()));
        addTakeMana();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, TAKE, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        // Hand shrank by the cast card, then grew by two draws.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 2);
    }

    @Test
    @DisplayName("Take on a creature with no counters draws nothing")
    void takeWithoutCountersDrawsNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiveTake()));
        addTakeMana();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, TAKE, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Take cannot target a creature an opponent controls")
    void takeCannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiveTake()));
        addTakeMana();

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, TAKE, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fuse resolves Give before Take, so counters given to a creature can be cashed in for cards")
    void fuseResolvesGiveThenTake() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiveTake()));
        addGiveMana();
        addTakeMana();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castModalSorcery(player1, 0, FUSE, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Fuse keeps the halves on their own targets")
    void fuseUsesIndependentTargets() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GiveTake()));
        addGiveMana();
        addTakeMana();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castModalSorcery(player1, 0, FUSE, List.of(opponentBears.getId(), ownBears.getId()));
        harness.passBothPriorities();

        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 1);
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiveTake()));
        addGiveMana();

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
