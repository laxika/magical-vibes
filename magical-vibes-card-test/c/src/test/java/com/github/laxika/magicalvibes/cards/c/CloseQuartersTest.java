package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.h.HornedTroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloseQuarters.class, HornedTroll.class, Disenchant.class})
class CloseQuartersTest extends BaseCardTest {

    @Test
    @DisplayName("A blocked creature you control triggers 1 damage to a chosen creature")
    void blockedAllyDealsDamageToChosenCreature() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        attacker.setAttacking(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HornedTroll());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A blocked creature you control triggers 1 damage to a chosen player")
    void blockedAllyDealsDamageToChosenPlayer() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HornedTroll());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A creature controlled by an opponent does not trigger Close Quarters")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player2, new HornedTroll());
        attacker.setAttacking(true);
        addCreatureReady(player1, new HornedTroll());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature blocked by multiple creatures triggers Close Quarters only once")
    void multipleBlockersTriggerOnlyOnce() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HornedTroll());
        addCreatureReady(player2, new HornedTroll());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An unblocked attacker does not trigger Close Quarters")
    void unblockedAttackerDoesNotTrigger() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HornedTroll());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each blocked attacker triggers Close Quarters separately")
    void twoBlockedAttackersDealTwoDamage() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent firstAttacker = addCreatureReady(player1, new HornedTroll());
        Permanent secondAttacker = addCreatureReady(player1, new HornedTroll());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        addCreatureReady(player2, new HornedTroll());
        addCreatureReady(player2, new HornedTroll());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 2)));

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Close Quarters can target its controller")
    void triggerCanDamageController() {
        harness.addToBattlefield(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HornedTroll());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The damage trigger resolves after Close Quarters is destroyed")
    void triggerResolvesAfterEnchantmentLeavesBattlefield() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new CloseQuarters());
        Permanent attacker = addCreatureReady(player1, new HornedTroll());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HornedTroll());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player2, 0, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Close Quarters");
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }
}
