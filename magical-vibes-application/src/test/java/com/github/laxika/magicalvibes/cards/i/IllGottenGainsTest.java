package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllGottenGains.class, DarkRitual.class, Swamp.class})
class IllGottenGainsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and returns up to three graveyard cards")
    void eachPlayerDiscardsAndReturnsUpToThreeCards() {
        IllGottenGains gains = new IllGottenGains();
        List<Card> player1Hand = List.of(
                gains, new DarkRitual(), new Swamp(),
                new DarkRitual(), new Swamp());
        List<Card> player2Hand = List.of(
                new DarkRitual(), new Swamp(), new DarkRitual(), new Swamp());
        castIllGottenGains(player1Hand, player2Hand);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);

        chooseThree(player1);
        chooseThree(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gains);
    }

    @Test
    @DisplayName("Each player may return fewer than three cards")
    void eachPlayerMayReturnFewerThanThreeCards() {
        IllGottenGains gains = new IllGottenGains();
        castIllGottenGains(
                List.of(gains, new DarkRitual(), new Swamp(), new DarkRitual(), new Swamp()),
                List.of());

        chooseCards(player1, 1);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gains);
    }

    @Test
    @DisplayName("Each player may return fewer than three cards when exactly three are available")
    void eachPlayerMayReturnFewerThanThreeCardsWhenExactlyThreeAreAvailable() {
        IllGottenGains gains = new IllGottenGains();
        castIllGottenGains(
                List.of(gains, new DarkRitual(), new Swamp(), new DarkRitual()),
                List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gains);
    }

    @Test
    @DisplayName("Exiles itself when neither player has cards in their graveyard")
    void exilesItselfWhenNeitherPlayerHasCardsInTheirGraveyard() {
        IllGottenGains gains = new IllGottenGains();
        castIllGottenGains(List.of(gains), List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gains);
    }

    @Test
    @DisplayName("Ill-Gotten Gains is already exiled when players choose cards to return")
    void exilesItselfBeforeGraveyardChoices() {
        IllGottenGains gains = new IllGottenGains();
        castIllGottenGains(List.of(gains, new DarkRitual()), List.of(new Swamp()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gains);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gains);

        harness.handleGraveyardCardChosen(player1, -1);
        harness.handleGraveyardCardChosen(player2, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsOnly(gains);
    }

    @Test
    @DisplayName("Cards remain in graveyards until both players have finished choosing")
    void returnsChosenCardsSimultaneouslyAfterAllPlayersChoose() {
        DarkRitual first = new DarkRitual();
        Swamp second = new Swamp();
        DarkRitual opponentCard = new DarkRitual();
        castIllGottenGains(List.of(new IllGottenGains(), first, second), List.of(opponentCard));

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);

        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Players with empty hands can return cards already in their graveyards")
    void returnsPreexistingGraveyardCardsWithEmptyHands() {
        IllGottenGains gains = new IllGottenGains();
        DarkRitual ritual = new DarkRitual();
        Swamp swamp = new Swamp();
        DarkRitual opponentRitual = new DarkRitual();
        harness.setGraveyard(player1, List.of(ritual, swamp));
        harness.setGraveyard(player2, List.of(opponentRitual));

        castIllGottenGains(List.of(gains), List.of());
        chooseCards(player1, 2);
        chooseCards(player2, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(ritual, swamp);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentRitual);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsOnly(gains);
    }

    private void castIllGottenGains(List<Card> player1Hand, List<Card> player2Hand) {
        harness.setHand(player1, player1Hand);
        harness.setHand(player2, player2Hand);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void chooseThree(Player player) {
        chooseCards(player, 3);
    }

    private void chooseCards(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.handleGraveyardCardChosen(player, 0);
        }
    }
}
