package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinSwineRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked deals 2 damage to each attacking and each blocking creature")
    void becomingBlockedDamagesAllAttackersAndBlockers() {
        Permanent swine = addAttackingSwine(player1);
        TestCards.mutableCard(swine).setToughness(3);

        Permanent otherAttacker = new Permanent(new GrizzlyBears());
        otherAttacker.setSummoningSick(false);
        otherAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(otherAttacker);
        TestCards.mutableCard(otherAttacker).setToughness(3);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        TestCards.mutableCard(blocker).setToughness(3);

        Permanent idle = new Permanent(new GrizzlyBears());
        idle.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(idle);

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
        Permanent swine = addAttackingSwine(player1);

        Permanent otherAttacker = new Permanent(new GrizzlyBears());
        otherAttacker.setSummoningSick(false);
        otherAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(otherAttacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Swine-Rider");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Goblin Swine-Rider");
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

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Swine-Rider");
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(swine.getId());
    }

    private Permanent addAttackingSwine(Player player) {
        Permanent perm = new Permanent(new GoblinSwineRider());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
