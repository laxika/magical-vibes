package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Snort.class})
class SnortTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage only to opponents who accepted the discard and draw")
    void damagesOnlyAcceptingOpponents() {
        Card player1HandCard = new Snort();
        Card player2HandCard = new Snort();
        harness.setHand(player1, List.of(new Snort(), player1HandCard));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);
        addMana(player1, 3, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(player2HandCard);
    }

    @Test
    @DisplayName("Flashback resolves Snort and exiles it")
    void flashbackResolvesAndExilesSpell() {
        Snort spell = new Snort();
        Card player2HandCard = new Snort();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player2, List.of(player2HandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Snort");
    }

    @Test
    @DisplayName("Both players discard and draw, but only the opponent takes damage")
    void bothPlayersAccept() {
        Snort spell = new Snort();
        Card ownHandCard = new Snort();
        Card opponentHandCard = new Snort();
        harness.setHand(player1, List.of(spell, ownHandCard));
        harness.setHand(player2, List.of(opponentHandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);
        addMana(player1, 3, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownHandCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        harness.assertLife(player2, 20);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownHandCard, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentHandCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An opponent can discard an empty hand, draw five, and take damage")
    void acceptingWithEmptyHandStillDrawsAndTakesDamage() {
        harness.setHand(player2, List.of());
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);
        harness.castFromHand(player1, new Snort(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Snort");
    }

    @Test
    @DisplayName("Declining leaves both hands and libraries unchanged and deals no damage")
    void bothPlayersDecline() {
        Card ownHandCard = new Snort();
        Card opponentHandCard = new Snort();
        harness.setHand(player1, List.of(new Snort(), ownHandCard));
        harness.setHand(player2, List.of(opponentHandCard));
        fillLibrary(player1, 10);
        fillLibrary(player2, 10);
        addMana(player1, 3, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownHandCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHandCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Snort");
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player, int colorless, int red) {
        harness.addMana(player, ManaColor.COLORLESS, colorless);
        harness.addMana(player, ManaColor.RED, red);
    }

    private void fillLibrary(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Snort());
        }
        harness.setLibrary(player, cards);
    }
}
