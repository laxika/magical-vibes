package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AladdinsLamp.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        Swamp.class})
class AladdinsLampTest extends BaseCardTest {

    private void activateLamp(int x) {
        activateLamp(0, x);
    }

    private void activateLamp(int permanentIndex, int x) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, x);
        harness.activateAbility(player1, permanentIndex, x, null);
        harness.passBothPriorities(); // resolve the ability — registers the delayed next-draw replacement
    }

    private List<Card> deck() {
        return gd.playerDecks.get(player1.getId());
    }

    private List<String> handNames() {
        return gd.playerHands.get(player1.getId()).stream().map(Card::getName).toList();
    }

    @Test
    @DisplayName("Next draw looks at the top X cards; the chosen one is drawn, the rest go to the bottom")
    void digReplacesNextDraw() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new Forest(), new Swamp(), new GrizzlyBears(), new HillGiant()));

        activateLamp(3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        // Look at the top 3 cards: [Llanowar Elves, Forest, Swamp].
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1)); // keep Forest, draw it

        assertThat(handNames()).containsExactly("Forest");
        // The untouched card below the looked-at three is now on top.
        assertThat(deck().getFirst().getName()).isEqualTo("Grizzly Bears");
        // The two unchosen looked-at cards are on the bottom (order is random).
        List<String> bottomTwo = deck().subList(deck().size() - 2, deck().size())
                .stream().map(Card::getName).toList();
        assertThat(bottomTwo).containsExactlyInAnyOrder("Llanowar Elves", "Swamp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the next draw is replaced — a later draw is an ordinary draw")
    void replacementIsOneShot() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new Forest(), new Swamp(), new GrizzlyBears()));

        activateLamp(2);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0)); // keep Llanowar Elves

        // A second draw is ordinary — no look-at interaction is offered.
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Llanowar Elves", "Swamp");
    }

    @Test
    @DisplayName("X of 1 just draws the top card — nothing goes to the bottom")
    void xOfOneDrawsNormally() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest()));

        activateLamp(1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("If the library has fewer than X cards, all available cards are considered")
    void xLargerThanLibraryUsesAvailableCards() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest()));

        activateLamp(5);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(handNames()).containsExactly("Forest");
        assertThat(deck()).hasSize(1);
        assertThat(deck().getFirst().getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("Multiple Lamp activations replace successive draws in the same draw event")
    void multipleReplacementsChain() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new Forest(), new Swamp(), new GrizzlyBears(), new HillGiant()));

        activateLamp(0, 2);
        activateLamp(1, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        PendingInteraction.LibrarySearch firstSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(firstSearch).isNotNull();
        assertThat(firstSearch.params().cards()).hasSizeBetween(2, 3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().cards()).hasSizeBetween(2, 3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(handNames()).hasSize(1);
        assertThat(deck()).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X cannot be 0")
    void cannotChooseZeroForX() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must be at least 1");
    }

    @Test
    @CardUsed(ThoughtReflection.class)
    @DisplayName("The draw generated by an X = 1 replacement can be replaced again")
    void xOfOneAllowsAnotherDrawReplacement() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.addToBattlefield(player1, new ThoughtReflection());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest(), new Swamp()));

        activateLamp(1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(handNames()).containsExactly("Llanowar Elves", "Forest");
        assertThat(deck()).hasSize(1);
    }

    @Test
    @DisplayName("The delayed replacement expires at end of turn if the player never draws")
    void replacementExpiresAtCleanup() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Forest(), new Swamp()));

        activateLamp(3);

        // End the turn without drawing — the "this turn" replacement expires at cleanup.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Llanowar Elves");
    }
}
