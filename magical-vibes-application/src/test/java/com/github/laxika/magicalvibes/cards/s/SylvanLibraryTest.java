package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanLibraryTest extends BaseCardTest {

    private Card bears;
    private Card elves;
    private Card shock;
    private Card filler1;
    private Card filler2;

    private void setup() {
        bears = new GrizzlyBears();
        elves = new LlanowarElves();
        shock = new Shock();
        filler1 = new GrizzlyBears();
        filler2 = new GrizzlyBears();

        harness.addToBattlefield(player1, new SylvanLibrary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(bears, elves, shock, filler1, filler2));
        harness.setLife(player1, 20);
    }

    /** Advances player1 to their draw step, which draws bears and fires the Sylvan Library trigger. */
    private void advanceToDrawAndTrigger() {
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances UPKEEP -> DRAW, runs the draw step
        harness.passBothPriorities(); // resolve the draw-step MayEffect from the stack -> may prompt
    }

    private List<Card> hand() {
        return gd.playerHands.get(player1.getId());
    }

    private List<Card> library() {
        return gd.playerDecks.get(player1.getId());
    }

    @Test
    @DisplayName("Accepting draws two additional cards and prompts the resolve choice")
    void acceptingDrawsTwoAndPrompts() {
        setup();
        advanceToDrawAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // Normal draw (bears) + two additional (elves, shock) are all in hand.
        assertThat(hand()).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), elves.getId(), shock.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SylvanLibraryChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SylvanLibraryChoice.class).resolveCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Putting both chosen cards on top of the library costs no life")
    void putBothOnTop() {
        setup();
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));

        // First chosen (bears) ends up nearest the top, then elves.
        assertThat(library().get(0).getId()).isEqualTo(bears.getId());
        assertThat(library().get(1).getId()).isEqualTo(elves.getId());
        assertThat(hand()).extracting(Card::getId).containsExactly(shock.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Putting no cards back pays 4 life per resolved card and keeps them in hand")
    void payForBoth() {
        setup();
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(hand()).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), elves.getId(), shock.getId());
        harness.assertLife(player1, 12); // 20 - 4 - 4
    }

    @Test
    @DisplayName("Topping one card pays 4 life for the other resolved card")
    void topOnePayOne() {
        setup();
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(library().get(0).getId()).isEqualTo(shock.getId());
        assertThat(hand()).extracting(Card::getId).containsExactlyInAnyOrder(bears.getId(), elves.getId());
        harness.assertLife(player1, 16); // 20 - 4
    }

    @Test
    @DisplayName("Declining the may draws only the normal card and prompts no choice")
    void decliningMay() {
        setup();
        advanceToDrawAndTrigger();

        harness.handleMayAbilityChosen(player1, false);

        // Only the normal turn-based draw happened; no extra draws and no resolve choice.
        assertThat(hand()).extracting(Card::getId).containsExactly(bears.getId());
        harness.assertLife(player1, 20);
    }
}
