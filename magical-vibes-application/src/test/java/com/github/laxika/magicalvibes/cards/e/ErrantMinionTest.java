package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrantMinionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature's controller with no mana takes the full 2 damage")
    void noManaTakesFullDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachErrantMinion(creature);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Paying 1 mana prevents 1 damage, so the controller takes 1")
    void paysOneTakesOne() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachErrantMinion(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // resolve trigger -> prompts for payment
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Paying 2 mana prevents all 2 damage")
    void paysTwoTakesNone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachErrantMinion(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Paying 0 mana takes the full 2 damage and spends nothing")
    void paysZeroTakesFull() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachErrantMinion(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Payment prompt is capped at 2 even with more mana available")
    void promptCappedAtTwo() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachErrantMinion(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passBothPriorities(); // resolve trigger -> prompt

        PendingInteraction.XValueChoice ctx =
                (PendingInteraction.XValueChoice) gd.interaction.activeInteraction();
        assertThat(ctx).isNotNull();
        assertThat(ctx.playerId()).isEqualTo(player2.getId());
        assertThat(ctx.maxValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Errant Minion does NOT trigger during the aura controller's own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachErrantMinion(creature);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void attachErrantMinion(Permanent creature) {
        Permanent errantMinion = new Permanent(new ErrantMinion());
        errantMinion.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(errantMinion);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
