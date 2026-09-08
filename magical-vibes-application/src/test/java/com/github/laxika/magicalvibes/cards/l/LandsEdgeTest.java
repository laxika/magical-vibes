package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LandsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when a land card is discarded")
    void dealsDamageWhenLandIsDiscarded() {
        addLandsEdge();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Discards a nonland card but deals no damage")
    void nonlandDiscardDealsNoDamage() {
        addLandsEdge();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent may activate the ability using their own hand")
    void opponentMayActivate() {
        addLandsEdge();
        harness.setLife(player1, 20);
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, List.of(new Mountain()));

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCard() {
        addLandsEdge();
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature as a target before paying the discard cost")
    void rejectsCreatureTargetBeforePayingCost() {
        addLandsEdge();
        harness.setHand(player1, List.of(new Mountain()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent addLandsEdge() {
        return harness.addToBattlefieldAndReturn(player1, new LandsEdge());
    }
}
