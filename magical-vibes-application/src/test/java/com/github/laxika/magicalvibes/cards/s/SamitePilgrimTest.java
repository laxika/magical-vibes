package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamitePilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents five damage with all five basic land types")
    void preventsDamageBasedOnAllBasicLandTypes() {
        Permanent pilgrim = addReadyPilgrim();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activate(pilgrim, target);

        assertThat(target.getDamagePreventionShield()).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts distinct basic land types rather than lands")
    void countsDistinctBasicLandTypes() {
        Permanent pilgrim = addReadyPilgrim();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activate(pilgrim, target);

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents the next amount of combat damage to the target creature")
    void preventsCombatDamageToTargetCreature() {
        Permanent pilgrim = addReadyPilgrim();
        harness.addToBattlefield(player1, new Plains());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        activate(pilgrim, blocker);

        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent pilgrim = addReadyPilgrim();
        int pilgrimIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pilgrim);

        assertThatThrownBy(() -> harness.activateAbility(player1, pilgrimIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPilgrim() {
        Permanent pilgrim = harness.addToBattlefieldAndReturn(player1, new SamitePilgrim());
        pilgrim.setSummoningSick(false);
        return pilgrim;
    }

    private void activate(Permanent pilgrim, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int pilgrimIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pilgrim);
        harness.activateAbility(player1, pilgrimIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
