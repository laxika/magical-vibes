package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BountyOfSkemfar.class, Forest.class, LlanowarElves.class,
        GrizzlyBears.class, Shock.class})
class BountyOfSkemfarTest extends BaseCardTest {

    @Test
    @DisplayName("Offers at most one land and one Elf from the top six")
    void offersLandAndElfChoices() {
        Card forest = new Forest();
        Card elf = new LlanowarElves();
        Card secondForest = new Forest();
        setLibrary(forest, new GrizzlyBears(), elf, new Shock(), secondForest, new Shock());

        castAndResolve();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactly(forest, secondForest);
        assertThat(choice.params().destination()).isEqualTo(
                com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Puts the chosen land tapped, the Elf into hand, and the rest on the bottom")
    void putsLandAndElfInTheirDestinations() {
        Card forest = new Forest();
        Card elf = new LlanowarElves();
        Card bear = new GrizzlyBears();
        Card shock = new Shock();
        Card secondForest = new Forest();
        Card secondShock = new Shock();
        setLibrary(forest, bear, elf, shock, secondForest, secondShock);

        castAndResolve();
        chooseLibraryCard(0);

        PendingInteraction.LibrarySearch elfChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(elfChoice.params().cards()).containsExactly(elf);
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(elf).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(bear, shock, secondForest, secondShock);
    }

    @Test
    @DisplayName("Declining the land still offers the Elf choice")
    void maySkipLandAndTakeElf() {
        Card forest = new Forest();
        Card elf = new LlanowarElves();
        Card bear = new GrizzlyBears();
        Card shock = new Shock();
        Card secondForest = new Forest();
        Card secondShock = new Shock();
        setLibrary(forest, bear, elf, shock, secondForest, secondShock);

        castAndResolve();
        chooseLibraryCard(-1);

        PendingInteraction.LibrarySearch elfChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(elfChoice.params().cards()).containsExactly(elf);
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest || permanent.getCard() == elf);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, bear, shock, secondForest, secondShock);
    }

    @Test
    @DisplayName("Can decline both choices and bottom all six cards")
    void mayDeclineBothChoices() {
        Card forest = new Forest();
        Card elf = new LlanowarElves();
        Card bear = new GrizzlyBears();
        Card shock = new Shock();
        Card secondForest = new Forest();
        Card secondShock = new Shock();
        List<Card> topCards = List.of(forest, bear, elf, shock, secondForest, secondShock);
        setLibrary(topCards.toArray(Card[]::new));

        castAndResolve();
        chooseLibraryCard(-1);
        chooseLibraryCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContainAnyElementsOf(topCards);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> topCards.contains(permanent.getCard()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new BountyOfSkemfar()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void chooseLibraryCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
