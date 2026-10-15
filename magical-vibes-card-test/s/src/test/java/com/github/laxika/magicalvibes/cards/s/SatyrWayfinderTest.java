package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SatyrWayfinder.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class SatyrWayfinderTest extends BaseCardTest {

    @Test
    @DisplayName("Only land cards among the revealed four are offered")
    void offersOnlyLands() {
        setupTopFour(new Forest(), new GrizzlyBears(), new HillGiant(), new Forest());

        resolveWayfinder();

        GameData data = harness.getGameData();
        assertThat(data.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(data)).containsExactly("Forest", "Forest");
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Taking a land puts it in hand and the rest into the graveyard")
    void takingLandRestToGraveyard() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card giant = new HillGiant();
        Card bears2 = new GrizzlyBears();
        setupTopFour(bears, forest, giant, bears2);

        resolveWayfinder();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, giant, bears2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining bins all four into the graveyard")
    void decliningBinsEverything() {
        setupTopFour(new Forest(), new GrizzlyBears(), new HillGiant(), new Forest());

        resolveWayfinder();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears", "Hill Giant", "Forest");
    }

    @Test
    @DisplayName("With no lands revealed, all four go to the graveyard with no prompt")
    void noLandBinsDirectly() {
        setupTopFour(new GrizzlyBears(), new HillGiant(), new GrizzlyBears(), new HillGiant());

        resolveWayfinder();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A short library reveals all remaining cards and still allows taking a land")
    void shortLibraryAllowsTakingLand() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, forest));

        resolveWayfinder();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a card movement")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());

        resolveWayfinder();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Satyr Wayfinder");
    }

    @Test
    @DisplayName("Only one land is kept and cards below the top four stay in order")
    void keepsOnlyOneLandAndLeavesRemainingLibraryInOrder() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card fifth = new Forest();
        Card sixth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstLand, bears, secondLand, giant, fifth, sixth));

        resolveWayfinder();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(firstLand, secondLand);
        chooseCard(1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondLand);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, bears, giant);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, sixth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveWayfinder() {
        harness.castFromHand(player1, new SatyrWayfinder(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature spell → enters trigger on stack
        harness.passBothPriorities(); // resolve the trigger
    }

    private void chooseCard(int index) {
        harness.handleCardChosen(player1, index);
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFour(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
