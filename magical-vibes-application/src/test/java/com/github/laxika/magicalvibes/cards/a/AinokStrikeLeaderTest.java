package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZurgoStormrender;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AinokStrikeLeader.class, GrizzlyBears.class, ZurgoStormrender.class})
class AinokStrikeLeaderTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepAttackTriggersInCombat() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.DECLARE_ATTACKERS));
    }

    @Test
    @DisplayName("Attacking with Ainok Strike Leader creates a tapped and attacking Goblin")
    void leaderAttackCreatesGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent goblin = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attacking with your commander creates a Goblin even if the leader stays back")
    void commanderAttackCreatesGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());
        Permanent commander = addCreatureReady(player1, new ZurgoStormrender());
        gd.makeCommander(player1.getId(), commander.getCard());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Attacking with both the leader and your commander creates only one Goblin per opponent")
    void leaderAndCommanderAttackTriggersOnlyOnce() {
        addCreatureReady(player1, new AinokStrikeLeader());
        Permanent commander = addCreatureReady(player1, new ZurgoStormrender());
        gd.makeCommander(player1.getId(), commander.getCard());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttacking()).isTrue();
        assertThat(goblin.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Sacrificing the attacking leader does not stop its trigger or protect later tokens")
    void sacrificingLeaderInResponseStillCreatesUnprotectedGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());

        declareAttackers(List.of(0));
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Ainok Strike Leader");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttacking()).isTrue();
        assertThat(goblin.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, goblin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent attacking with their commander does not trigger your leader")
    void opponentsCommanderDoesNotTriggerLeader() {
        addCreatureReady(player1, new AinokStrikeLeader());
        Permanent commander = addCreatureReady(player2, new ZurgoStormrender());
        gd.makeCommander(player2.getId(), commander.getCard());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Attacking with a stolen opponent's commander does not trigger the leader")
    void stolenCommanderDoesNotTriggerLeader() {
        addCreatureReady(player1, new AinokStrikeLeader());
        Permanent commander = addCreatureReady(player2, new ZurgoStormrender());
        gd.makeCommander(player2.getId(), commander.getCard());
        harness.inMutationScope(() -> com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(com.github.laxika.magicalvibes.service.battlefield.CreatureControlService.class)
                .applyControlEffect(gd, player1.getId(), commander,
                        new com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect(
                                com.github.laxika.magicalvibes.model.effect.ControlDuration.PERMANENT),
                        com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT, null, "Test setup"));
        commander.setSummoningSick(false);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("An unrelated creature attack does not trigger Ainok Strike Leader")
    void unrelatedAttackDoesNotCreateGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the leader grants indestructible to your creature tokens until end of turn")
    void sacrificeProtectsCreatureTokens() {
        Permanent leader = addCreatureReady(player1, new AinokStrikeLeader());
        Permanent nonToken = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(leader), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonToken, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Ainok Strike Leader");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
