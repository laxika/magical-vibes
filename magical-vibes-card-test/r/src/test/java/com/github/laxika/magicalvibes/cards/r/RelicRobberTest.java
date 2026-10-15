package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelicRobber.class})
class RelicRobberTest extends BaseCardTest {

    @Test
    void damagedPlayerCreatesTheConstruct() {
        Permanent robber = addCreatureReady(player1, new RelicRobber());
        robber.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Construct")).isEmpty();
        assertThat(findPermanents(player2, "Goblin Construct")).hasSize(1);
    }

    @Test
    void constructCannotBlock() {
        Permanent construct = createConstructForPlayer2();

        assertThat(bls.canBlock(gd, construct)).isFalse();
    }

    @Test
    void constructDamagesItsControllerAtUpkeep() {
        createConstructForPlayer2();
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void constructDoesNotTriggerDuringOpponentsUpkeep() {
        createConstructForPlayer2();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void constructTriggersAgainOnLaterUpkeepsWithoutRobber() {
        createConstructForPlayer2();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void eachRobberCreatesOneConstructAndEachConstructDealsDamage() {
        Permanent first = addCreatureReady(player1, new RelicRobber());
        Permanent second = addCreatureReady(player1, new RelicRobber());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Goblin Construct")).hasSize(2);
        harness.setLife(player2, 20);
        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    void hasteAllowsAttackingOnTheTurnRobberEnters() {
        Permanent robber = harness.addToBattlefieldAndReturn(player1, new RelicRobber());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(robber.isAttacking()).isTrue();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Goblin Construct")).hasSize(1);
    }

    private Permanent createConstructForPlayer2() {
        Permanent robber = addCreatureReady(player1, new RelicRobber());
        robber.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();
        resolveAllTriggers();

        return findPermanents(player2, "Goblin Construct").getFirst();
    }
}
