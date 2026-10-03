package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuergarAssailant.class, GrizzlyBears.class, LlanowarElves.class})
class DuergarAssailantTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target attacking creature")
    void dealsDamageToAttacker() {
        harness.addToBattlefield(player1, new DuergarAssailant());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        // 1 damage kills the 1/1 attacker
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Deals 1 damage to a target blocking creature")
    void dealsDamageToBlocker() {
        harness.addToBattlefield(player1, new DuergarAssailant());
        Permanent blocker = addBlocker(player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Duergar Assailant is sacrificed as part of the cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new DuergarAssailant());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Duergar Assailant");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player1, new DuergarAssailant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("An attacker removed from combat is illegal when the ability resolves")
    void doesNotDamageCreatureThatStopsAttacking() {
        harness.addToBattlefield(player1, new DuergarAssailant());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Duergar Assailant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Assailant can activate its sacrifice ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DuergarAssailant());
        source.setSummoningSick(true);
        source.tap();
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Duergar Assailant");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @CardUsed({DuergarAssailant.class})
    @DisplayName("Can target an attacking creature controlled by its own controller")
    void canDamageFriendlyAttacker() {
        harness.addToBattlefield(player1, new DuergarAssailant());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DuergarAssailant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Duergar Assailant");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Duergar Assailant"))
                .hasSize(2);
    }

    private Permanent addAttacker(Player owner) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, new LlanowarElves());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = harness.addToBattlefieldAndReturn(owner, new LlanowarElves());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
