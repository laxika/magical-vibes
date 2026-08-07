package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaturesWillTest extends BaseCardTest {

    private Permanent addTappedLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        land.tap();
        return land;
    }

    private Permanent addUntappedLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
        land.untap();
        return land;
    }

    private Permanent addAttackingBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        return bears;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        // Resolve the trigger directly: passing priority again would auto-cascade past the
        // opponent's untap step, which would untap the lands the trigger just tapped.
        if (!gd.stack.isEmpty()) {
            harness.getStackResolutionService().resolveTopOfStack(gd);
        }
    }

    @Test
    @DisplayName("Combat damage to a player taps their lands and untaps yours")
    void tapsDefendersLandsAndUntapsOwn() {
        harness.addToBattlefield(player1, new NaturesWill());
        Permanent myLand = addTappedLand(player1);
        Permanent theirLand = addUntappedLand(player2);
        addAttackingBears();

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(myLand.isTapped()).isFalse();
        assertThat(theirLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only lands are affected — non-land permanents keep their tap state")
    void onlyLandsAffected() {
        harness.addToBattlefield(player1, new NaturesWill());
        Permanent myTappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        myTappedCreature.tap();
        Permanent theirCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addAttackingBears();

        runCombatDamage();

        assertThat(myTappedCreature.isTapped()).isTrue();
        assertThat(theirCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No trigger when the attacker is blocked and deals no damage to the player")
    void noTriggerWhenBlocked() {
        harness.addToBattlefield(player1, new NaturesWill());
        Permanent myLand = addTappedLand(player1);
        Permanent theirLand = addUntappedLand(player2);
        Permanent bears = addAttackingBears();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(bears.getId());

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(theirLand.isTapped()).isFalse();
        assertThat(myLand.isTapped()).isTrue();
    }
}
