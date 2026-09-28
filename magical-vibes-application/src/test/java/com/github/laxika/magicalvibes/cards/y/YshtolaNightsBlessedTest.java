package com.github.laxika.magicalvibes.cards.y;

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

@CardUsed({YshtolaNightsBlessed.class, GrizzlyBears.class, JacesSanctum.class, Shock.class})
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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void dealTwoDamage(Player target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
