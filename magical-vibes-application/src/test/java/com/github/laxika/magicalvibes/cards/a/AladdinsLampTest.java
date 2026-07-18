package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AladdinsLampTest extends BaseCardTest {

    private void activateLamp(int x) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, x);
        harness.activateAbility(player1, 0, x, null);
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
                new LlanowarElves(), new Shock(), new Plains(), new GrizzlyBears(), new HillGiant()));

        activateLamp(3);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        // Look at the top 3 cards: [Llanowar Elves, Shock, Plains].
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1)); // keep Shock, draw it

        assertThat(handNames()).containsExactly("Shock");
        // The untouched card below the looked-at three is now on top.
        assertThat(deck().getFirst().getName()).isEqualTo("Grizzly Bears");
        // The two unchosen looked-at cards are on the bottom (order is random).
        List<String> bottomTwo = deck().subList(deck().size() - 2, deck().size())
                .stream().map(Card::getName).toList();
        assertThat(bottomTwo).containsExactlyInAnyOrder("Llanowar Elves", "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the next draw is replaced — a later draw is an ordinary draw")
    void replacementIsOneShot() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new LlanowarElves(), new Shock(), new Plains(), new GrizzlyBears()));

        activateLamp(2);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0)); // keep Llanowar Elves

        // A second draw is ordinary — no look-at interaction is offered.
        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Llanowar Elves", "Plains");
    }

    @Test
    @DisplayName("X of 1 just draws the top card — nothing goes to the bottom")
    void xOfOneDrawsNormally() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock()));

        activateLamp(1);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("The delayed replacement expires at end of turn if the player never draws")
    void replacementExpiresAtCleanup() {
        harness.addToBattlefield(player1, new AladdinsLamp());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock(), new Plains()));

        activateLamp(3);

        // End the turn without drawing — the "this turn" replacement expires at cleanup.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(handNames()).containsExactly("Llanowar Elves");
    }
}
