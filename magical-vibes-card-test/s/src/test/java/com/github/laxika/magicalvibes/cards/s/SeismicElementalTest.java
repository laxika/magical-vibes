package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RingwardenOwl;
import com.github.laxika.magicalvibes.cards.c.Cobblebrute;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicElemental.class, Cobblebrute.class, RingwardenOwl.class})
class SeismicElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures without flying can't block after the ETB trigger resolves")
    void nonFliersCantBlock() {
        Permanent bears = addCreatureReady(player2, new Cobblebrute());

        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new Cobblebrute());

        assertThat(bls.canBlockAttacker(gd, bears, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Creatures with flying are unaffected")
    void fliersUnaffected() {
        Permanent airElemental = addCreatureReady(player2, new RingwardenOwl());

        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent attackerForPlayer1 = addCreatureReady(player1, new Cobblebrute());

        assertThat(bls.canBlockAttacker(gd, airElemental, attackerForPlayer1,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Affects both players' non-flying creatures")
    void affectsBothPlayers() {
        Permanent ownBears = addCreatureReady(player1, new Cobblebrute());
        Permanent oppBears = addCreatureReady(player2, new Cobblebrute());

        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, ownBears, oppBears,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, oppBears, ownBears,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("A non-flying creature cannot be declared as a blocker")
    void nonFlierCannotBeDeclaredBlocker() {
        Permanent attacker = addCreatureReady(player1, new Cobblebrute());
        addCreatureReady(player2, new Cobblebrute());

        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flying creature can still block")
    void flierCanStillBlock() {
        Permanent attacker = addCreatureReady(player1, new Cobblebrute());
        Permanent blocker = addCreatureReady(player2, new RingwardenOwl());

        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves cannot block")
    void laterCreaturesCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new Cobblebrute());
        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        resolveAllTriggers();

        Permanent blocker = harness.enterBattlefieldAndReturn(player2, new Cobblebrute());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("The restriction starts only when the ETB trigger resolves")
    void restrictionWaitsForTriggerResolution() {
        Permanent attacker = addCreatureReady(player1, new Cobblebrute());
        Permanent blocker = addCreatureReady(player2, new Cobblebrute());
        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        resolveAllTriggers();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new Cobblebrute());
        Permanent blocker = addCreatureReady(player2, new Cobblebrute());
        harness.castFromHand(player1, new SeismicElemental(), "{3}{R}{R}");
        resolveAllTriggers();

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
