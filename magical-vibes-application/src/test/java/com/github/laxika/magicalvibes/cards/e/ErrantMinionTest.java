package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantMinion.class, BalduvianBears.class, Island.class})
class ErrantMinionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature's controller with no mana takes the full 2 damage")
    void noManaTakesFullDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        attachErrantMinion(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The controller may pay more mana than the damage amount")
    void allowsPayingMoreThanDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        attachErrantMinion(creature);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.passBothPriorities(); // resolve trigger -> prompt

        PendingInteraction.XValueChoice ctx =
                (PendingInteraction.XValueChoice) gd.interaction.activeInteraction();
        assertThat(ctx).isNotNull();
        assertThat(ctx.playerId()).isEqualTo(player2.getId());
        assertThat(ctx.maxValue()).isEqualTo(5);

        harness.handleXValueChosen(player2, 3);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("pays {3} to prevent 3 damage from Errant Minion"));
    }

    @Test
    @DisplayName("Errant Minion does NOT trigger during the aura controller's own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        attachErrantMinion(creature);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Errant Minion cannot enchant a noncreature permanent")
    void cannotEnchantNonCreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ErrantMinion()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachErrantMinion(Permanent creature) {
        Permanent errantMinion = harness.addToBattlefieldAndReturn(player1, new ErrantMinion());
        errantMinion.setAttachedTo(creature.getId());
    }
}
