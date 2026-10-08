package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.c.Claustrophobia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesSanctum;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YshtolaNightsBlessed.class, GrizzlyBears.class, JacesSanctum.class, Shock.class, Blaze.class, Claustrophobia.class})
class YshtolaNightsBlessedTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at the end step when any player lost four life")
    void drawsAtEndStepAfterFourLifeLost() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        dealTwoDamage(player2);
        dealTwoDamage(player2);

        advanceToEndStep(player1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not draw at the end step when fewer than four life was lost")
    void doesNotDrawBelowLifeLossThreshold() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        dealTwoDamage(player2);

        advanceToEndStep(player1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals two damage to each opponent and gains two life for an expensive noncreature spell")
    void expensiveNoncreatureSpellDamagesAndGainsLife() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new JacesSanctum()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature spell with mana value less than three")
    void lowManaValueNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Draws during an opponent's end step after four life was lost")
    void drawsAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        dealTwoDamage(player2);
        dealTwoDamage(player2);

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller losing four life also satisfies the draw condition")
    void drawsAfterControllerLostFourLife() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        dealTwoDamage(player1);
        dealTwoDamage(player1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Life loss by different players is not added together")
    void doesNotCombineLifeLossAcrossPlayers() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        dealTwoDamage(player1);
        dealTwoDamage(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Reaching four life lost after the end step begins does not trigger a draw")
    void lifeLossAfterEndStepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        dealTwoDamage(player2);
        advanceToEndStep(player1);

        dealTwoDamage(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature spell with exactly three mana value triggers before resolving")
    void exactlyThreeManaValueTriggers() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Claustrophobia()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Claustrophobia");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A creature spell with mana value four does not trigger")
    void expensiveCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player2, new YshtolaNightsBlessed());
        harness.setHand(player2, List.of(new YshtolaNightsBlessed()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent casting an expensive noncreature spell does not trigger")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JacesSanctum()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chosen X counts toward the mana value of a spell on the stack")
    void xValueCountsTowardManaValue() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An X spell below three mana value does not trigger")
    void smallXValueDoesNotTrigger() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Gaining life back does not undo life lost earlier in the turn")
    void lifeGainDoesNotUndoLifeLoss() {
        harness.addToBattlefield(player1, new YshtolaNightsBlessed());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        dealTwoDamage(player1);
        dealTwoDamage(player1);
        harness.setHand(player1, List.of(new JacesSanctum()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }
    private void dealTwoDamage(Player target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
