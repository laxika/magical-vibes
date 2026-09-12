package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BubblingMuck;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherSting.class, HulkingOgre.class, BubblingMuck.class})
class AetherStingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage when an opponent casts a creature spell")
    void opponentCreatureSpellDealsDamage() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new HulkingOgre(), "{2}{R}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's noncreature spell")
    void opponentNoncreatureSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new BubblingMuck(), "{B}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Does not trigger for the controller's creature spell")
    void controllerCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AetherSting());
        harness.castFromHand(player1, new HulkingOgre(), "{2}{R}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Each copy triggers when an opponent casts a creature spell")
    void eachCopyDealsDamage() {
        setUpOpponentTurn();
        harness.addToBattlefield(player1, new AetherSting());

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player2, new HulkingOgre(), "{2}{R}");

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new AetherSting());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
