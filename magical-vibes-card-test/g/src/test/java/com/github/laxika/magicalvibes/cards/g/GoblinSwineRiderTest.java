package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinSwineRider.class, Warthog.class, FuneralCharm.class})
class GoblinSwineRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked deals 2 damage to each attacking and each blocking creature")
    void becomingBlockedDamagesAllAttackersAndBlockers() {
        Permanent swine = addAttackingSwine(player1);
        TestCards.mutableCard(swine).setToughness(3);

        Permanent otherAttacker = addCreatureReady(player1, new Warthog());
        otherAttacker.setAttacking(true);
        TestCards.mutableCard(otherAttacker).setToughness(3);

        Permanent blocker = addCreatureReady(player2, new Warthog());
        TestCards.mutableCard(blocker).setToughness(3);

        Permanent idle = addCreatureReady(player2, new Warthog());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(swine.getMarkedDamage()).isEqualTo(2);
        assertThat(otherAttacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked kills 1- and 2-toughness attackers and blockers including itself")
    void becomingBlockedKillsFragileCombatCreatures() {
        addAttackingSwine(player1);

        Permanent otherAttacker = addCreatureReady(player1, new Warthog());
        otherAttacker.setAttacking(true);

        addCreatureReady(player2, new Warthog());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Swine-Rider");
        harness.assertNotOnBattlefield(player1, "Warthog");
        harness.assertNotOnBattlefield(player2, "Warthog");
        harness.assertInGraveyard(player1, "Goblin Swine-Rider");
    }

    @Test
    @DisplayName("Becoming blocked triggers once even when blocked by multiple creatures")
    void becomingBlockedTriggersOnceWithMultipleBlockers() {
        Permanent swine = addAttackingSwine(player1);
        TestCards.mutableCard(swine).setToughness(3);

        Permanent firstBlocker = addCreatureReady(player2, new Warthog());
        TestCards.mutableCard(firstBlocker).setToughness(3);
        Permanent secondBlocker = addCreatureReady(player2, new Warthog());
        TestCards.mutableCard(secondBlocker).setToughness(3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(swine.getMarkedDamage()).isEqualTo(2);
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(2);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when Goblin Swine-Rider is unblocked")
    void doesNotTriggerWhenUnblocked() {
        Permanent swine = addAttackingSwine(player1);
        TestCards.mutableCard(swine).setToughness(3);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(swine.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(swine.getId()));
    }

    @Test
    @DisplayName("Becoming blocked pushes one triggered ability onto the stack")
    void becomingBlockedPushesTrigger() {
        Permanent swine = addAttackingSwine(player1);

        addCreatureReady(player2, new Warthog());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(swine.getId());
    }

    @Test
    @DisplayName("The blocked trigger still damages combat creatures after its source dies")
    void triggerResolvesAfterSourceDies() {
        Permanent swine = addAttackingSwine(player1);
        Permanent otherAttacker = addCreatureReady(player1, new Warthog());
        otherAttacker.setAttacking(true);
        addCreatureReady(player2, new Warthog());
        Permanent idle = addCreatureReady(player2, new Warthog());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player1, List.of(new FuneralCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, 1, swine.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Swine-Rider");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Warthog");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(idle);
        assertThat(idle.getMarkedDamage()).isZero();
    }

    private Permanent addAttackingSwine(Player player) {
        Permanent perm = addCreatureReady(player, new GoblinSwineRider());
        perm.setAttacking(true);
        return perm;
    }
}
