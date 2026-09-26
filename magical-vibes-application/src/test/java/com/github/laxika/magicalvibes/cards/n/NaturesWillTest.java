package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturesWill.class, Forest.class, HumbleBudoka.class})
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
        Permanent bears = addCreatureReady(player1, new HumbleBudoka());
        bears.setAttacking(true);
        return bears;
    }

    private void runCombatDamage() {
        resolveCombat();
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
        Permanent myTappedCreature = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        myTappedCreature.tap();
        Permanent theirCreature = harness.addToBattlefieldAndReturn(player2, new HumbleBudoka());
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

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HumbleBudoka());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(bears.getId());

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(theirLand.isTapped()).isFalse();
        assertThat(myLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple creatures dealing damage together create only one trigger")
    void oneTriggerForMultipleCombatDamageDealers() {
        harness.addToBattlefield(player1, new NaturesWill());
        addAttackingBears();
        addAttackingBears();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.stack).hasSize(1);
    }
}
