package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Slithermuse.class, IndomitableAncients.class})
class SlithermuseTest extends BaseCardTest {

    private List<Card> ancients(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new IndomitableAncients());
        }
        return cards;
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        resolveLeaveBattlefieldTrigger();
    }

    private void returnToHand(Permanent permanent) {
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        resolveLeaveBattlefieldTrigger();
    }

    private void resolveLeaveBattlefieldTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // collect LTB trigger onto the stack
        resolveAllTriggers();
    }

    @Test
    @DisplayName("LTB: opponent holds more cards, controller draws the difference")
    void drawsDifferenceWhenOpponentHoldsMore() {
        harness.setHand(player1, ancients(1));
        harness.setHand(player2, ancients(4));
        harness.setLibrary(player1, ancients(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        leaveBattlefield(slithermuse);

        // difference = 4 - 1 = 3 drawn; hand 1 -> 4, library 5 -> 2.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("LTB: opponent holds no more than controller, no cards drawn")
    void drawsNothingWhenOpponentHoldsFewerOrEqual() {
        harness.setHand(player1, ancients(3));
        harness.setHand(player2, ancients(2));
        harness.setLibrary(player1, ancients(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        leaveBattlefield(slithermuse);

        // 2 - 3 is non-positive, so no draw.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB draws the hand-size difference")
    void evokeSacrificesThenDraws() {
        harness.setHand(player1, List.of(new Slithermuse()));
        harness.setHand(player2, ancients(3));
        harness.setLibrary(player1, ancients(5));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature -> ETB trigger on stack
        resolveAllTriggers();

        // After casting, player1's hand is empty; opponent holds 3 -> draw 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Slithermuse");
        harness.assertInGraveyard(player1, "Slithermuse");
    }

    @Test
    @DisplayName("LTB: a returned Slithermuse counts in its controller's hand")
    void countsReturnedSourceInControllerHand() {
        harness.setHand(player1, ancients(1));
        harness.setHand(player2, ancients(4));
        harness.setLibrary(player1, ancients(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        returnToHand(slithermuse);

        // The returned Slithermuse is in hand when the trigger resolves: 4 - (1 + 1) = 2 drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Slithermuse");
    }

    @Test
    @DisplayName("Normal casting leaves Slithermuse on the battlefield without drawing")
    void normalCastingDoesNotSacrificeOrDraw() {
        harness.setHand(player1, List.of(new Slithermuse()));
        harness.setHand(player2, ancients(3));
        harness.setLibrary(player1, ancients(5));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Slithermuse");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Equal hand sizes draw no cards")
    void drawsNothingWhenHandSizesAreEqual() {
        harness.setHand(player1, ancients(3));
        harness.setHand(player2, ancients(3));
        harness.setLibrary(player1, ancients(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        leaveBattlefield(slithermuse);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("The trigger draws if the opponent gains more cards before resolution")
    void usesHandSizesAtResolutionWhenOpponentGainsCards() {
        harness.setHand(player1, ancients(3));
        harness.setHand(player2, ancients(2));
        harness.setLibrary(player1, ancients(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slithermuse));
        harness.setHand(player2, ancients(5));
        resolveLeaveBattlefieldTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The trigger draws nothing if its controller gains enough cards before resolution")
    void usesHandSizesAtResolutionWhenControllerGainsCards() {
        harness.setHand(player1, ancients(1));
        harness.setHand(player2, ancients(4));
        harness.setLibrary(player1, ancients(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, slithermuse));
        harness.setHand(player1, ancients(4));
        resolveLeaveBattlefieldTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A Slithermuse owned by the opponent draws for its battlefield controller")
    void drawsForControllerRatherThanOwner() {
        harness.setHand(player1, ancients(4));
        harness.setHand(player2, ancients(1));
        harness.setLibrary(player2, ancients(5));
        Slithermuse card = new Slithermuse();
        card.setOwnerId(player1.getId());
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player2, card);

        leaveBattlefield(slithermuse);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Slithermuse");
    }
}
