package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.f.FontOfAgonies;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanLibrary.class, GrizzlyBears.class, LlanowarElves.class})
class SylvanLibraryTest extends BaseCardTest {

    private Card bears;
    private Card elves;
    private Card thirdCard;
    private Card filler1;
    private Card filler2;

    private void setup() {
        bears = new GrizzlyBears();
        elves = new LlanowarElves();
        thirdCard = new GrizzlyBears();
        filler1 = new GrizzlyBears();
        filler2 = new GrizzlyBears();

        harness.addToBattlefield(player1, new SylvanLibrary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(bears, elves, thirdCard, filler1, filler2));
        harness.setLife(player1, 20);
    }

    /** Advances player1 to their draw step, which performs the turn-based draw and fires triggers. */
    private void advanceToDrawStep() {
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.DRAW);
    }

    /** Advances player1 to their draw step and resolves the Sylvan Library may prompt. */
    private void advanceToDrawAndTrigger() {
        advanceToDrawStep();
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

        // Normal draw (bears) + two additional (elves, thirdCard) are all in hand.
        assertThat(hand()).extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), elves.getId(), thirdCard.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SylvanLibraryChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SylvanLibraryChoice.class).resolveCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Sylvan Libraries resolve their draw choices one at a time")
    void multipleLibrariesResolveInSequence() {
        setup();
        harness.addToBattlefield(player1, new SylvanLibrary());
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .contains("Sylvan Library");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hand()).hasSize(3);
        harness.assertLife(player1, 12);
    }

    @Test
    @CardUsed(NarsetParterOfVeils.class)
    @DisplayName("A draw-prevention effect still leaves the normal card to resolve")
    void resolvesWithOnlyTheNormalCardWhenExtraDrawsArePrevented() {
        setup();
        Permanent narset = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        narset.setCounterCount(CounterType.LOYALTY, 5);
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hand()).extracting(Card::getId).containsExactly(bears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SylvanLibraryChoice.class).resolveCount())
                .isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player1, 16);
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
        assertThat(hand()).extracting(Card::getId).containsExactly(thirdCard.getId());
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
                .containsExactlyInAnyOrder(bears.getId(), elves.getId(), thirdCard.getId());
        harness.assertLife(player1, 12); // 20 - 4 - 4
    }

    @Test
    @DisplayName("Topping one card pays 4 life for the other resolved card")
    void topOnePayOne() {
        setup();
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(thirdCard.getId()));

        assertThat(library().get(0).getId()).isEqualTo(thirdCard.getId());
        assertThat(hand()).extracting(Card::getId).containsExactlyInAnyOrder(bears.getId(), elves.getId());
        harness.assertLife(player1, 16); // 20 - 4
    }

    @Test
    @DisplayName("Cards that were already in hand cannot be chosen")
    void ignoresCardsThatWereAlreadyInHand() {
        setup();
        Card heldCard = new GrizzlyBears();
        harness.setHand(player1, List.of(heldCard));
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of(heldCard.getId(), bears.getId()));

        assertThat(library().get(0).getId()).isEqualTo(bears.getId());
        assertThat(hand()).extracting(Card::getId)
                .containsExactlyInAnyOrder(heldCard.getId(), elves.getId(), thirdCard.getId());
        harness.assertLife(player1, 16);
    }

    @Test
    @CardUsed(Abundance.class)
    @DisplayName("Each replaced extra draw is resolved before choosing Sylvan Library cards")
    void resolvesDrawReplacementsBeforeLibraryChoice() {
        setup();
        harness.addToBattlefield(player1, new Abundance());
        advanceToDrawStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .contains("Abundance");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .contains("Abundance");
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).description())
                .contains("Abundance");
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SylvanLibraryChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(hand()).hasSize(3);
        harness.assertLife(player1, 12);
    }

    @Test
    @CardUsed(FontOfAgonies.class)
    @DisplayName("Paying for kept cards triggers effects that watch life payments")
    void payingForKeptCardsTriggersLifePayment() {
        setup();
        harness.addToBattlefield(player1, new FontOfAgonies());
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Font of Agonies").getCounterCount(CounterType.BLOOD))
                .isEqualTo(8);
        harness.assertLife(player1, 12);
    }

    @Test
    @CardUsed(PlatinumEmperion.class)
    @DisplayName("Cards must be put back when the controller cannot pay life")
    void cannotPayLifeWhenLifeTotalCannotChange() {
        setup();
        harness.addToBattlefield(player1, new PlatinumEmperion());
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(hand()).hasSize(1);
        assertThat(library()).hasSize(4);
        assertThat(library()).extracting(Card::getId)
                .containsAnyOf(bears.getId(), elves.getId(), thirdCard.getId());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An empty-library loss waits until the ability finishes resolving")
    void drawingFromEmptyLibraryLosesAfterFollowUpChoice() {
        setup();
        harness.setLibrary(player1, List.of(bears, elves));
        advanceToDrawAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.SylvanLibraryChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
