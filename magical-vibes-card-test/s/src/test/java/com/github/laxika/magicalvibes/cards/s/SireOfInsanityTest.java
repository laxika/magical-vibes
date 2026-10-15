package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SireOfInsanity.class, GrizzlyBears.class, LightningBolt.class})
class SireOfInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand at the end step")
    void bothPlayersDiscardHands() {
        addSire(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers at an opponent's end step too")
    void triggersOnOpponentEndStep() {
        addSire(player1);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToEndStepAndResolve(player2);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty hand does not prevent the other player from discarding")
    void emptyControllerHandDoesNotPreventOpponentDiscard() {
        addSire(player1);
        SireOfInsanity discarded = new SireOfInsanity();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(discarded));

        advanceToEndStepAndResolve(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    @DisplayName("The trigger discards cards in hand at resolution, not at triggering")
    void discardsCardsAddedAfterTriggering() {
        addSire(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        SireOfInsanity drawn = new SireOfInsanity();
        harness.setHand(player2, List.of(drawn));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger resolves even after Sire leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new SireOfInsanity());
        SireOfInsanity firstDiscard = new SireOfInsanity();
        SireOfInsanity secondDiscard = new SireOfInsanity();
        harness.setHand(player1, List.of(firstDiscard));
        harness.setHand(player2, List.of(secondDiscard));
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sire);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sire);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondDiscard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering after the end step begins does not trigger immediately")
    void enteringDuringEndStepDoesNotTrigger() {
        harness.setHand(player1, List.of());
        SireOfInsanity held = new SireOfInsanity();
        harness.setHand(player2, List.of(held));
        advanceToEndStep(player1);

        harness.enterBattlefieldAndReturn(player1, new SireOfInsanity());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(held);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void addSire(Player player) {
        harness.addToBattlefield(player, new SireOfInsanity());
    }
}
