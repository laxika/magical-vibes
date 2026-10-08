package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.v.VioletPall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnstoppableAsh.class, GrizzlyBears.class, ElvishWarrior.class, BoskBanneret.class, VioletPall.class})
class UnstoppableAshTest extends BaseCardTest {

    @Test
    @DisplayName("When another creature you control becomes blocked, it gets +0/+5 until end of turn")
    void allyBecomesBlockedGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        addCreatureReady(player1, new UnstoppableAsh());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Unstoppable Ash");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(bears.getId());
        assertThat(trigger.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        assertThat(bears.getToughnessModifier()).isEqualTo(5);
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Unstoppable Ash boosts itself when it becomes blocked")
    void selfBecomesBlockedGetsBoost() {
        Permanent ash = addCreatureReady(player1, new UnstoppableAsh());
        ash.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ash.getToughnessModifier()).isEqualTo(5);
        assertThat(ash.getEffectiveToughness()).isEqualTo(10);
    }

    @Test
    @DisplayName("No becomes-blocked trigger when the creature is unblocked")
    void unblockedCreatesNoTrigger() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        addCreatureReady(player1, new UnstoppableAsh());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Champion ETB auto-sacrifices with no Treefolk or Warrior to exile")
    void championAutoSacrificesWithoutValidCreature() {
        castAsh();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Unstoppable Ash");
        harness.assertInGraveyard(player1, "Unstoppable Ash");
    }

    @Test
    @DisplayName("Champion exiles a Warrior and keeps Unstoppable Ash")
    void championExilesWarrior() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        castAsh();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID warriorId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.handlePermanentChosen(player1, warriorId);

        harness.assertOnBattlefield(player1, "Unstoppable Ash");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elvish Warrior"));
    }

    @Test
    @DisplayName("Champion can exile a Treefolk and returns it when Ash leaves")
    void championTreefolkReturnsWhenAshLeaves() {
        harness.addToBattlefield(player1, new BoskBanneret());
        castAsh();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Bosk Banneret"));

        harness.assertNotOnBattlefield(player1, "Bosk Banneret");
        harness.assertOnBattlefield(player1, "Unstoppable Ash");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bosk Banneret"));

        harness.setHand(player1, List.of(new VioletPall()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Unstoppable Ash"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Bosk Banneret");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bosk Banneret");
        harness.assertInGraveyard(player1, "Unstoppable Ash");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Being blocked by two creatures gives only one boost")
    void multipleBlockersTriggerOnlyOnce() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        warrior.setAttacking(true);
        addCreatureReady(player1, new UnstoppableAsh());
        addCreatureReady(player2, new ElvishWarrior());
        addCreatureReady(player2, new ElvishWarrior());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(warrior.getToughnessModifier()).isEqualTo(5);
        assertThat(warrior.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Opponent's blocked creature does not receive Ash's boost")
    void opponentsBlockedCreatureDoesNotTrigger() {
        addCreatureReady(player1, new UnstoppableAsh());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    private void castAsh() {
        harness.setHand(player1, List.of(new UnstoppableAsh()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

}
