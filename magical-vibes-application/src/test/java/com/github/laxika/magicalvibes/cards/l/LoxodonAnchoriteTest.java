package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonAnchorite.class, LoxodonStalwart.class})
class LoxodonAnchoriteTest extends BaseCardTest {

    @Test
    void preventsTheNextTwoDamageToTargetCreature() {
        Permanent anchorite = addCreatureReady(player1, new LoxodonAnchorite());
        Permanent blocker = addCreatureReady(player2, new LoxodonStalwart());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, blocker.getId());
        assertThat(anchorite.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new LoxodonStalwart());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Loxodon Stalwart");
        harness.assertNotOnBattlefield(player1, "Loxodon Stalwart");
    }

    @Test
    void preventsTheNextTwoDamageToTargetPlayer() {
        Permanent anchorite = addCreatureReady(player2, new LoxodonAnchorite());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player2.getId());
        assertThat(anchorite.isTapped()).isTrue();
        harness.passBothPriorities();

        addCreatureReady(player1, new LoxodonStalwart());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    void targetPreventionShieldExpiresAtEndOfTurn() {
        addCreatureReady(player1, new LoxodonAnchorite());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addCreatureReady(player2, new LoxodonStalwart()).setAttacking(true);
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }
}
