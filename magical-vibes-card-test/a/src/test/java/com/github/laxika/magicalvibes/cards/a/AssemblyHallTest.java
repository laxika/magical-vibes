package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeepwoodWolverine;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
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

@CardUsed({AssemblyHall.class, Forest.class, DeepwoodWolverine.class, PsychogenicProbe.class})
class AssemblyHallTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals a creature in hand and searches for a card with the same name")
    void revealsCreatureAndSearchesForSameName() {
        addAssemblyHallAndMana();
        harness.setHand(player1, List.of(new DeepwoodWolverine()));
        harness.setLibrary(player1, List.of(new Forest(), new DeepwoodWolverine()));

        activateAndResolve();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Deepwood Wolverine");

        harness.handleListChoice(player1, "Deepwood Wolverine");

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Deepwood Wolverine");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Deepwood Wolverine", "Deepwood Wolverine");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature card in hand remains there after the search")
    void revealedCreatureRemainsInHandWhenSearchFails() {
        addAssemblyHallAndMana();
        harness.setHand(player1, List.of(new DeepwoodWolverine()));
        harness.setLibrary(player1, List.of(new Forest()));

        activateAndResolve();
        harness.handleListChoice(player1, "Deepwood Wolverine");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Deepwood Wolverine");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot choose a noncreature card from hand")
    void cannotChooseNoncreatureCard() {
        addAssemblyHallAndMana();
        harness.setHand(player1, List.of(new Forest()));

        activateAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Forest"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays and taps even when no creature card is available to reveal")
    void paysAndTapsWhenNoCreatureCardIsInHand() {
        addAssemblyHallAndMana();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        activateAndResolve();

        assertThat(findPermanent(player1, "Assembly Hall").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Still shuffles when there is no creature card to reveal")
    void shufflesWithoutCreatureInHand() {
        addAssemblyHallAndMana();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        activateAndResolve();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still shuffles with an empty hand and empty library")
    void shufflesWithEmptyHandAndLibrary() {
        addAssemblyHallAndMana();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        activateAndResolve();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May fail to find a matching card and still shuffles")
    void mayDeclineMatchingCardAndStillShuffle() {
        addAssemblyHallAndMana();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        Card original = new DeepwoodWolverine();
        Card matching = new DeepwoodWolverine();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(matching, new Forest()));

        activateAndResolve();
        harness.handleListChoice(player1, "Deepwood Wolverine");
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerDecks.get(player1.getId())).contains(matching).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Successful search shuffles and takes only one matching card")
    void successfulSearchTakesOnlyOneCardAndShuffles() {
        addAssemblyHallAndMana();
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        Card original = new DeepwoodWolverine();
        Card first = new DeepwoodWolverine();
        Card second = new DeepwoodWolverine();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(first, second));

        activateAndResolve();
        harness.handleListChoice(player1, "Deepwood Wolverine");
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Chooses from the current hand during resolution and ignores noncreatures")
    void choosesCreatureDuringResolution() {
        addAssemblyHallAndMana();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new DeepwoodWolverine()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.setHand(player1, List.of(new Forest(), new DeepwoodWolverine(), new DeepwoodWolverine()));
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Deepwood Wolverine");
        harness.handleListChoice(player1, "Deepwood Wolverine");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addAssemblyHallAndMana() {
        harness.addToBattlefield(player1, new AssemblyHall());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
