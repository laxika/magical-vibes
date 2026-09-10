package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeekTheWilds.class, LlanowarElves.class, Forest.class, Plains.class, Shock.class})
class SeekTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Offers creature and land cards among the top four")
    void offersCreatureAndLandCards() {
        setupTopCards(new LlanowarElves(), new Shock(), new Forest(), new Plains());

        resolveSeekTheWilds();

        assertThat(searchCards(gd)).containsExactlyInAnyOrder("Llanowar Elves", "Forest", "Plains");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand and the rest on the library bottom")
    void choosingCreaturePutsItIntoHand() {
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card forest = new Forest();
        Card plains = new Plains();
        setupTopCards(elves, shock, forest, plains);

        resolveSeekTheWilds();
        chooseCard(searchCards(gd).indexOf("Llanowar Elves"));

        assertThat(gd.playerHands.get(player1.getId())).contains(elves);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrder(shock, forest, plains);
    }

    @Test
    @DisplayName("Declining the optional card puts all four cards on the library bottom")
    void decliningPutsAllCardsOnBottom() {
        setupTopCards(new LlanowarElves(), new Shock(), new Forest(), new Plains());

        resolveSeekTheWilds();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("With no creature or land among the top four, all cards go directly to the bottom")
    void noMatchingCardsGoDirectlyToBottom() {
        setupTopCards(new Shock(), new Shock(), new Shock(), new Shock());

        resolveSeekTheWilds();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    private void resolveSeekTheWilds() {
        harness.setHand(player1, List.of(new SeekTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopCards(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
