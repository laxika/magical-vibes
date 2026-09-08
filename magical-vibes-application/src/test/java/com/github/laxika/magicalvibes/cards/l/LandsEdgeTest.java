package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LandsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land deals 2 damage to the target player")
    void landDiscardDealsDamage() {
        setUpLandsEdge();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Discarding a nonland card does not deal damage")
    void nonlandDiscardDoesNotDealDamage() {
        setUpLandsEdge();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Any player may activate Lands' Edge")
    void anyPlayerMayActivate() {
        setUpLandsEdge();
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Lands' Edge cannot target a creature")
    void cannotTargetCreature() {
        setUpLandsEdge();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player or planeswalker");
    }

    private void setUpLandsEdge() {
        harness.addToBattlefield(player1, new LandsEdge());
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
    }
}
