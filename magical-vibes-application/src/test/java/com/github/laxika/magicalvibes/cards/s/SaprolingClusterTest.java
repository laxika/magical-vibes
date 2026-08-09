package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaprolingClusterTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays {1} and discards a card to create a Saproling")
    void controllerActivatesAbility() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("An opponent may pay {1} and discard a card to create a Saproling")
    void opponentActivatesAbility() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Saproling");
        harness.assertNotOnBattlefield(player1, "Saproling");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void requiresCardToDiscard() {
        harness.addToBattlefield(player1, new SaprolingCluster());
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
