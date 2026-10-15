package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScoutTheBorders.class, GrizzlyBears.class, Shock.class, Forest.class, HillGiant.class})
class ScoutTheBordersTest extends BaseCardTest {

    @Test
    void offersCreatureAndLandCardsAmongTheRevealedFive() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant(), new Shock());

        resolveScout();

        GameData data = harness.getGameData();
        assertThat(data.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(data)).containsExactlyInAnyOrder("Grizzly Bears", "Forest", "Hill Giant");
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().canFailToFind()).isTrue();
    }

    @Test
    void takingACreaturePutsItInHandAndTheRestIntoTheGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        Card giant = new HillGiant();
        Card shock2 = new Shock();
        setupTopFive(bears, shock, forest, giant, shock2);

        resolveScout();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest, giant, shock2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningPutsAllRevealedCardsIntoTheGraveyard() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant(), new Shock());

        resolveScout();
        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder(
                        "Grizzly Bears", "Shock", "Forest", "Hill Giant", "Shock", "Scout the Borders");
    }

    @Test
    void withNoCreatureOrLandAllRevealedCardsGoToTheGraveyardWithoutAPrompt() {
        setupTopFive(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());

        resolveScout();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void takingALandLeavesCardsBelowTheTopFiveUntouched() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card shock = new Shock();
        Card secondShock = new Shock();
        Card sixth = new Forest();
        Card seventh = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, forest, bears, giant, secondShock, sixth, seventh));

        resolveScout();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(bears, giant, shock, secondShock).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth, seventh);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withFewerThanFiveCardsUsesAllRemainingCards() {
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock, forest));

        resolveScout();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withAnEmptyLibraryResolvesWithoutAChoice() {
        harness.setLibrary(player1, List.of());

        resolveScout();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Scout the Borders");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveScout() {
        harness.setHand(player1, List.of(new ScoutTheBorders()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void chooseCard(int index) {
        harness.handleCardChosen(player1, index);
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFive(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
