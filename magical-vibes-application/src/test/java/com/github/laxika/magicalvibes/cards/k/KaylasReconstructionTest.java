package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaylasReconstruction.class, GrizzlyBears.class, MindStone.class, LlanowarElves.class,
        Shock.class, Forest.class, SerraAngel.class, BottleGnomes.class})
class KaylasReconstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to X eligible cards onto the battlefield and bottoms the rest randomly")
    void putsUpToXEligibleCardsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card mindStone = new MindStone();
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card forest = new Forest();
        Card angel = new SerraAngel();
        setUpGame(List.of(bears, mindStone, elves, shock, forest, angel));

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 2);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), mindStone.getId(), elves.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), mindStone.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Shock", "Forest", "Serra Angel");
    }

    @Test
    @DisplayName("With X equal to zero, puts all looked-at cards on the bottom")
    void withZeroXDoesNotPutCardsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card mindStone = new MindStone();
        Card shock = new Shock();
        setUpGame(List.of(bears, mindStone, shock));

        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Mind Stone", "Shock");
    }

    @Test
    void canDeclineAllEligibleCardsWithPositiveX() {
        Card bears = new GrizzlyBears();
        Card stone = new MindStone();
        setUpGame(List.of(bears, stone));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 2);

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, stone);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void looksAtOnlySevenAndLeavesDeeperCardsAboveTheRemainder() {
        Card gnomes = new BottleGnomes();
        Card bears = new GrizzlyBears();
        Card stone = new MindStone();
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card forest = new Forest();
        Card angel = new SerraAngel();
        Card eighth = new GrizzlyBears();
        Card ninth = new MindStone();
        setUpGame(List.of(gnomes, bears, stone, elves, shock, forest, angel, eighth, ninth));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 2);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                gnomes.getId(), bears.getId(), stone.getId(), elves.getId());
        harness.handleMultipleCardsChosen(player1, List.of(gnomes.getId()));

        harness.assertOnBattlefield(player1, "Bottle Gnomes");
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(eighth, ninth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 8))
                .containsExactlyInAnyOrder(bears, stone, elves, shock, forest, angel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void rejectsMoreThanXAndIneligibleSelectionsWithoutLosingTheChoice() {
        Card bears = new GrizzlyBears();
        Card stone = new MindStone();
        Card angel = new SerraAngel();
        setUpGame(List.of(bears, stone, angel));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(bears.getId(), stone.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(angel.getId()))).isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(stone.getId()));

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, angel);
    }

    @Test
    void noEligibleCardsAreBottomedWithoutAChoice() {
        Card angel = new SerraAngel();
        Card shock = new Shock();
        Card forest = new Forest();
        setUpGame(List.of(angel, shock, forest));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(angel, shock, forest);
    }

    @Test
    void emptyLibraryDoesNotPreventResolution() {
        setUpGame(List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void setUpGame(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new KaylasReconstruction()));
    }
}
