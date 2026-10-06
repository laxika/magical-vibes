package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.cards.o.Outnumber;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeekTheWilds.class, OranRiefInvoker.class, EvolvingWilds.class, Plains.class, Outnumber.class})
class SeekTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Offers creature and land cards among the top four")
    void offersCreatureAndLandCards() {
        setupTopCards(new OranRiefInvoker(), new Outnumber(), new EvolvingWilds(), new Plains());

        resolveSeekTheWilds();

        assertThat(searchCards(gd)).containsExactlyInAnyOrder("Oran-Rief Invoker", "Evolving Wilds", "Plains");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand and the rest on the library bottom")
    void choosingCreaturePutsItIntoHand() {
        Card elves = new OranRiefInvoker();
        Card shock = new Outnumber();
        Card forest = new EvolvingWilds();
        Card plains = new Plains();
        setupTopCards(elves, shock, forest, plains);

        resolveSeekTheWilds();
        chooseCard(searchCards(gd).indexOf("Oran-Rief Invoker"));

        assertThat(gd.playerHands.get(player1.getId())).contains(elves);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactlyInAnyOrder(shock, forest, plains);
    }

    @Test
    @DisplayName("Declining the optional card puts all four cards on the library bottom")
    void decliningPutsAllCardsOnBottom() {
        setupTopCards(new OranRiefInvoker(), new Outnumber(), new EvolvingWilds(), new Plains());

        resolveSeekTheWilds();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("With no creature or land among the top four, all cards go directly to the bottom")
    void noMatchingCardsGoDirectlyToBottom() {
        setupTopCards(new Outnumber(), new Outnumber(), new Outnumber(), new Outnumber());

        resolveSeekTheWilds();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Choosing a nonbasic land preserves untouched cards and orders the rest on the bottom")
    void choosingLandOrdersRestBelowUntouchedCards() {
        Card creature = new OranRiefInvoker();
        Card instant = new Outnumber();
        Card land = new EvolvingWilds();
        Card plains = new Plains();
        Card untouched = new OranRiefInvoker();
        setupTopCards(creature, instant, land, plains, untouched);

        resolveSeekTheWilds();
        chooseCard(searchCards(gd).indexOf("Evolving Wilds"));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, plains, creature, instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card == creature || card == instant);
    }

    @Test
    @DisplayName("A matching fifth card is not offered and remains above the reordered top four")
    void doesNotLookBeyondTopFour() {
        Card first = new Outnumber();
        Card second = new Outnumber();
        Card third = new Outnumber();
        Card fourth = new Outnumber();
        Card fifth = new Plains();
        setupTopCards(first, second, third, fourth, fifth);

        resolveSeekTheWilds();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, fourth, third, second, first);
    }

    @Test
    @DisplayName("With fewer than four cards, a land can be chosen and the single remaining card is bottomed")
    void shortLibraryAllowsLandChoice() {
        Card instant = new Outnumber();
        Card land = new Plains();
        setupTopCards(instant, land);

        resolveSeekTheWilds();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The only eligible card in a one-card library may still be declined")
    void singleEligibleCardCanBeDeclined() {
        Card creature = new OranRiefInvoker();
        setupTopCards(creature);

        resolveSeekTheWilds();
        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without drawing or requesting a choice")
    void emptyLibraryResolvesWithoutChoice() {
        setupTopCards();

        resolveSeekTheWilds();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void resolveSeekTheWilds() {
        harness.setHand(player1, List.of(new SeekTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.handleCardChosen(player1, index);
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopCards(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
