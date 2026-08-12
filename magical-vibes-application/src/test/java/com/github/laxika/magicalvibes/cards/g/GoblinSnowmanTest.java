package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinSnowmanTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking prevents all combat damage dealt to and by Goblin Snowman")
    void blockingPreventsCombatDamageBothWays() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());

        blockWithSnowman();
        resolveAllTriggers();
        resolveCombat();

        assertThat(snowman.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Goblin Snowman");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without blocking, Goblin Snowman deals and takes combat damage normally")
    void unblockedCombatDamageIsNotPrevented() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());
        attacker.setAttacking(true);

        // Snowman is on the battlefield but never declared as a blocker, so no trigger fires.
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(snowman.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to the creature Goblin Snowman is blocking")
    void tapAbilityDamagesBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GoblinSnowman());

        blockWithSnowman();
        resolveAllTriggers();
        harness.activateAbility(player2, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability cannot target a creature Goblin Snowman isn't blocking")
    void tapAbilityCannotTargetUnblockedCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GoblinSnowman());
        otherAttacker.setAttacking(true);

        blockWithSnowman();
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncombat damage to Goblin Snowman is not prevented after it blocks")
    void nonCombatDamageToSnowmanIsNotPrevented() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent snowman = addCreatureReady(player2, new GoblinSnowman());

        blockWithSnowman();
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, snowman.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Snowman");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    /** Declares player1's first creature as an attacker and blocks it with player2's Goblin Snowman. */
    private void blockWithSnowman() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
