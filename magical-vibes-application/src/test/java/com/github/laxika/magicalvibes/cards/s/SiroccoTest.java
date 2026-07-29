package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SiroccoTest extends BaseCardTest {

    private void castSiroccoOn(int targetLife, List<Card> targetHand) {
        harness.setLife(player2, targetLife);
        harness.setHand(player2, new ArrayList<>(targetHand));

        harness.setHand(player1, List.of(new Sirocco()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying 4 life keeps the revealed blue instant")
    void paysLifeKeepsCard() {
        castSiroccoOn(20, List.of(new Peek(), new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining discards that blue instant; non-matching cards stay in hand")
    void declineDiscardsOnlyMatchingCard() {
        castSiroccoOn(20, List.of(new Peek(), new GrizzlyBears(), new LightningBolt()));

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each blue instant is a separate decision")
    void oneDecisionPerBlueInstant() {
        castSiroccoOn(20, List.of(new Peek(), new Counterspell()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        // Second prompt for the other blue instant.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A player who can't pay 4 life discards with no prompt")
    void cannotPayDiscardsAutomatically() {
        castSiroccoOn(3, List.of(new Peek(), new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 3);
        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No blue instants revealed — nothing is discarded")
    void noMatchingCardsNoEffect() {
        castSiroccoOn(20, List.of(new GrizzlyBears(), new LightningBolt()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
