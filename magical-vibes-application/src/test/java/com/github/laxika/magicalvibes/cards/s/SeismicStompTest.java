package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.n.NephaliaSeakite;
import com.github.laxika.magicalvibes.cards.z.ZephyrCharge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicStomp.class, CoralMerfolk.class, NephaliaSeakite.class, ZephyrCharge.class})
class SeismicStompTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures without flying can't block this turn")
    void nonFliersCantBlock() {
        Permanent bears = addCreatureReady(player2, new CoralMerfolk());

        castSeismicStomp();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new CoralMerfolk());

        assertThat(bls.canBlockAttacker(gd, bears, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Creatures with flying are unaffected")
    void fliersUnaffected() {
        Permanent drake = addCreatureReady(player2, new NephaliaSeakite());

        castSeismicStomp();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new CoralMerfolk());

        assertThat(bls.canBlockAttacker(gd, drake, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Applies to both players' non-flying creatures")
    void affectsAllPlayers() {
        Permanent ownBears = addCreatureReady(player1, new CoralMerfolk());
        Permanent oppBears = addCreatureReady(player2, new CoralMerfolk());
        Permanent oppDrake = addCreatureReady(player2, new NephaliaSeakite());

        castSeismicStomp();

        assertThat(bls.canBlockAttacker(gd, ownBears, oppBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppDrake, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("A restricted non-flier can't be declared as a blocker")
    void restrictedCreatureCantBeDeclaredBlocker() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        addCreatureReady(player2, new CoralMerfolk());

        castSeismicStomp();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flier can still be declared as a blocker")
    void flierCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        Permanent drake = addCreatureReady(player2, new NephaliaSeakite());

        castSeismicStomp();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Non-flying creatures entering after resolution cannot block")
    void laterNonFlierCantBlock() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        castSeismicStomp();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Flying creatures entering after resolution can block")
    void laterFlierCanBlock() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        castSeismicStomp();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NephaliaSeakite());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("A creature gaining flying after resolution can block")
    void gainingFlyingAllowsBlocking() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        harness.addToBattlefield(player2, new ZephyrCharge());
        castSeismicStomp();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 1, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        castSeismicStomp();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castSeismicStomp() {
        harness.setHand(player1, List.of(new SeismicStomp()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, (UUID) null);
    }

}
