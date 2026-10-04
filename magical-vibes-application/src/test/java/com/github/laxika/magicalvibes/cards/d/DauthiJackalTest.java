package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiJackal.class, RagingGoblin.class})
class DauthiJackalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target blocking creature")
    void destroysBlockingCreature() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        Permanent blocker = addBlocker();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Dauthi Jackal is sacrificed as part of the activation cost")
    void sacrificedAsCost() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        Permanent blocker = addBlocker();

        harness.activateAbility(player1, 0, null, blocker.getId());

        harness.assertNotOnBattlefield(player1, "Dauthi Jackal");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        addCreatureReady(player2, new RagingGoblin());

        var targetId = harness.getPermanentId(player2, "Raging Goblin");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");

        harness.assertOnBattlefield(player1, "Dauthi Jackal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with only one black mana")
    void cannotActivateWithoutTwoBlackMana() {
        addCreatureReady(player1, new DauthiJackal());
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent blocker = addBlocker();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        harness.assertOnBattlefield(player1, "Dauthi Jackal");
        harness.assertOnBattlefield(player2, "Raging Goblin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Jackal can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent jackal = addCreatureReady(player1, new DauthiJackal());
        jackal.tap();
        jackal.setSummoningSick(true);
        addBlackMana(player1);
        Permanent blocker = addBlocker();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dauthi Jackal");
        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Can destroy a blocking creature controlled by the ability's controller")
    void canDestroyOwnBlockingCreature() {
        addCreatureReady(player2, new RagingGoblin());
        addCreatureReady(player1, new DauthiJackal());
        Permanent blocker = addCreatureReady(player1, new RagingGoblin());
        addBlackMana(player1);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dauthi Jackal");
        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Does not destroy a target that stops blocking before resolution")
    void targetMustStillBeBlockingOnResolution() {
        addCreatureReady(player1, new DauthiJackal());
        addBlackMana(player1);
        Permanent blocker = addBlocker();

        harness.activateAbility(player1, 0, null, blocker.getId());
        blocker.clearCombatState();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dauthi Jackal");
        harness.assertOnBattlefield(player2, "Raging Goblin");
        assertThat(gd.stack).isEmpty();
    }

    private void addBlackMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
    }

    @Test
    @DisplayName("A creature without shadow cannot block Dauthi Jackal")
    void cannotBeBlockedByCreatureWithoutShadow() {
        addCreatureReady(player1, new DauthiJackal());
        addCreatureReady(player2, new RagingGoblin());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Dauthi Jackal cannot block a creature without shadow")
    void cannotBlockCreatureWithoutShadow() {
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player2, new DauthiJackal());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Can sacrifice an attacking Jackal to destroy its shadow blocker")
    void canDestroyShadowBlockerWhileAttacking() {
        addCreatureReady(player1, new DauthiJackal());
        Permanent blocker = addCreatureReady(player2, new DauthiJackal());
        addBlackMana(player1);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dauthi Jackal");
        harness.assertInGraveyard(player2, "Dauthi Jackal");
    }

    private Permanent addBlocker() {
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        Permanent blocker = addCreatureReady(player2, new RagingGoblin());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        return blocker;
    }
}
