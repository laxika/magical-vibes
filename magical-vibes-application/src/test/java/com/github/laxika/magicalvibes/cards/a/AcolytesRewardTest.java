package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AcolytesRewardTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage to the first target and deals it to the second target player")
    void preventsDamageToTargetCreatureAndRedirectsToPlayer() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Devotion determines the prevention amount and excess damage is dealt normally")
    void devotionAmountLeavesExcessDamage() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());

        Permanent attacker = addAttacker(new HillGiant());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The second target can be a creature")
    void redirectsToTargetCreature() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent redirectCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addWhiteDevotion(2);
        castReward(protectedCreature, redirectCreature.getId());

        Permanent attacker = addAttacker(new HillGiant());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void shieldExpiresAtEndOfTurn() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addWhiteDevotion(2);
        castReward(protectedCreature, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Zero white devotion prevents no damage")
    void zeroDevotionCreatesNoPrevention() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        castReward(protectedCreature, player2.getId());

        Permanent attacker = addAttacker(new GrizzlyBears());
        block(protectedCreature, attacker);
        runCombatDamage();

        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void addWhiteDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
    }

    private void castReward(Permanent protectedCreature, UUID redirectTargetId) {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AcolytesReward()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), redirectTargetId));
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        return attacker;
    }

    private void block(Permanent blocker, Permanent attacker) {
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player2.getId()).indexOf(attacker));
    }

    private void runCombatDamage() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
