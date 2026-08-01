package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrislySalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Offers creature and land cards among the revealed five")
    void offersCreatureOrLand() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant(), new Shock());

        resolveSalvage();

        GameData data = harness.getGameData();
        assertThat(data.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(data)).containsExactlyInAnyOrder("Grizzly Bears", "Forest", "Hill Giant");
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Taking a creature puts it in hand and the rest into the graveyard")
    void takingCreatureRestToGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        Card giant = new HillGiant();
        Card shock2 = new Shock();
        setupTopFive(bears, shock, forest, giant, shock2);

        resolveSalvage();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(shock, forest, giant, shock2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Taking a land puts it in hand and the rest into the graveyard")
    void takingLandRestToGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        setupTopFive(bears, shock, forest, new Shock(), new Shock());

        resolveSalvage();

        GameData data = harness.getGameData();
        int forestIndex = searchCards(data).indexOf("Forest");
        chooseCard(forestIndex);

        assertThat(data.playerHands.get(player1.getId())).contains(forest);
        assertThat(data.playerGraveyards.get(player1.getId())).contains(bears, shock);
    }

    @Test
    @DisplayName("Declining still bins all five into the graveyard")
    void decliningBinsEverything() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant(), new Shock());

        resolveSalvage();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Shock", "Forest", "Hill Giant", "Shock");
    }

    @Test
    @DisplayName("With a single eligible card, the may-pick is still offered")
    void singleEligibleStillOffersMayPick() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Shock(), new Shock(), new Shock());

        resolveSalvage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("With no creatures or lands, all five go to the graveyard")
    void noEligibleBinsDirectly() {
        setupTopFive(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        resolveSalvage();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Game log records the public reveal")
    void gameLogRecordsReveal() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new Shock(), new Shock());

        resolveSalvage();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Grisly Salvage"));
    }

    private void resolveSalvage() {
        harness.setHand(player1, List.of(new GrislySalvage()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFive(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
