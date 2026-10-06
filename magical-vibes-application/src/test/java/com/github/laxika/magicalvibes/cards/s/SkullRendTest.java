package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullRend.class, DrudgeBeetle.class, AxebaneStag.class})
class SkullRendTest extends BaseCardTest {

    private void castSkullRend() {
        harness.setHand(player1, List.of(new SkullRend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
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
        harness.setHand(player2, new ArrayList<>(List.of(new DrudgeBeetle(), new AxebaneStag(), new SkullRend())));

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

    @Test
    @DisplayName("Opponent with one card discards it without a prompt")
    void opponentWithOneCardDiscardsIt() {
        DrudgeBeetle card = new DrudgeBeetle();
        harness.setHand(player2, List.of(card));
        harness.setLife(player2, 20);

        castSkullRend();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with exactly two cards discards both")
    void opponentWithTwoCardsDiscardsBoth() {
        DrudgeBeetle beetle = new DrudgeBeetle();
        AxebaneStag stag = new AxebaneStag();
        harness.setHand(player2, List.of(beetle, stag));

        castSkullRend();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(beetle, stag);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controller keeps cards in hand while opponent discards")
    void controllerKeepsCardsInHand() {
        DrudgeBeetle beetle = new DrudgeBeetle();
        AxebaneStag stag = new AxebaneStag();
        harness.setHand(player1, List.of(new SkullRend(), beetle, stag));
        harness.setHand(player2, List.of(new DrudgeBeetle(), new AxebaneStag()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(beetle, stag);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Skull Rend");
    }
}
