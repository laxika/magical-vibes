package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void resolveWayfinder() {
        harness.setHand(player1, List.of(new SatyrWayfinder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → enters trigger on stack
        harness.passBothPriorities(); // resolve the trigger
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFour(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
