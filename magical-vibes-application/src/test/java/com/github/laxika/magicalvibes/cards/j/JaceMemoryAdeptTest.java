package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JaceMemoryAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card for the controller and mills the target player one card")
    void plusOneDrawsAndMills() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(29);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(29);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("0 mills the target player ten cards")
    void zeroMillsTen() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player2, 30);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(10);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("0 can mill its own controller")
    void zeroCanTargetController() {
        addReadyJace(player1, 4);
        stockLibrary(player1, 30);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(10);
    }

    @Test
    @DisplayName("-7 makes every targeted player draw twenty cards")
    void ultimateDrawsTwentyForEachTargetPlayer() {
        Permanent jace = addReadyJace(player1, 7);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-7 can target a single player only")
    void ultimateWithOneTarget() {
        addReadyJace(player1, 7);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("-7 cannot be activated with insufficient loyalty")
    void ultimateNeedsSevenLoyalty() {
        addReadyJace(player1, 6);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private void stockLibrary(Player player, int count) {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player, library);
        harness.setHand(player, List.of());
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent perm = new Permanent(new JaceMemoryAdept());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
