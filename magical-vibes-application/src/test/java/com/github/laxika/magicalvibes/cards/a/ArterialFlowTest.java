package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.v.VampireBats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArterialFlowTest extends BaseCardTest {

    private void castArterialFlow() {
        harness.setHand(player1, List.of(new ArterialFlow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each opponent discards two cards")
    void eachOpponentDiscardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new GrizzlyBears())));

        castArterialFlow();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With a Vampire, each opponent loses 2 life and controller gains 2 life")
    void vampireAddsLifeRider() {
        harness.addToBattlefield(player1, new VampireBats());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>());

        castArterialFlow();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Without a Vampire, the life rider does not happen")
    void noVampireNoLifeRider() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>());

        castArterialFlow();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }
}
