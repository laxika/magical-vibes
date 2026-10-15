package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CreepingMold;
import com.github.laxika.magicalvibes.cards.g.GossamerChains;
import com.github.laxika.magicalvibes.cards.g.GriffinCanyon;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FemerefEnchantress.class, CreepingMold.class, GossamerChains.class, GriffinCanyon.class, NevinyrralsDisk.class})
class FemerefEnchantressTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent's enchantment is destroyed")
    void drawsWhenOpponentEnchantmentDestroyed() {
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player2, new GossamerChains());

        UUID enchantmentId = harness.getPermanentId(player2, "Gossamer Chains");

        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, enchantmentId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws a card when own enchantment is destroyed")
    void drawsWhenOwnEnchantmentDestroyed() {
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player1, new GossamerChains());

        UUID enchantmentId = harness.getPermanentId(player1, "Gossamer Chains");

        harness.setHand(player2, List.of(new CreepingMold()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, enchantmentId);

        assertThat(gd.stack).hasSize(1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not trigger when a non-enchantment dies")
    void doesNotTriggerOnNonEnchantment() {
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player2, new GriffinCanyon());

        UUID landId = harness.getPermanentId(player2, "Griffin Canyon");
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, landId);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an enchantment returns to hand")
    void doesNotTriggerWhenEnchantmentReturnsToHand() {
        Permanent attacker = addCreatureReady(player1, new FemerefEnchantress());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.addToBattlefield(player2, new GossamerChains());

        int playerOneHandBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(playerOneHandBefore);
    }

    @Test
    @DisplayName("Two Femeref Enchantresses each draw when an enchantment dies")
    void twoEachDraw() {
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player2, new GossamerChains());

        UUID enchantmentId = harness.getPermanentId(player2, "Gossamer Chains");

        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, enchantmentId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
    @Test
    @DisplayName("Draws for every enchantment destroyed simultaneously with the Enchantress")
    void drawsWhenDestroyedSimultaneouslyWithEnchantments() {
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player1, new GossamerChains());
        harness.addToBattlefield(player1, new NevinyrralsDisk());
        harness.addToBattlefield(player2, new GossamerChains());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Femeref Enchantress");
        harness.assertInGraveyard(player1, "Gossamer Chains");
        harness.assertInGraveyard(player2, "Gossamer Chains");
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Each Enchantress controller draws when the same enchantment dies")
    void eachControllerDrawsForSameEnchantment() {
        harness.addToBattlefield(player1, new FemerefEnchantress());
        harness.addToBattlefield(player2, new FemerefEnchantress());
        harness.addToBattlefield(player2, new GossamerChains());
        UUID enchantmentId = harness.getPermanentId(player2, "Gossamer Chains");
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, enchantmentId);
        int playerOneHandBefore = gd.playerHands.get(player1.getId()).size();
        int playerTwoHandBefore = gd.playerHands.get(player2.getId()).size();
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(playerOneHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(playerTwoHandBefore + 1);
    }
}
