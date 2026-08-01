package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkullRendTest extends BaseCardTest {

    private void castSkullRend() {
        harness.setHand(player1, List.of(new SkullRend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage to the opponent")
    void dealsDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>());

        castSkullRend();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent discards two cards at random with no prompt")
    void opponentDiscardsTwoAtRandom() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));

        castSkullRend();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller is not damaged and does not discard")
    void controllerUnaffected() {
        harness.setLife(player1, 20);
        harness.setHand(player2, new ArrayList<>());

        castSkullRend();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Empty-handed opponent still takes damage with no discard")
    void emptyHandOpponentStillTakesDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, new ArrayList<>());

        castSkullRend();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>());

        castSkullRend();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Skull Rend");
    }
}
