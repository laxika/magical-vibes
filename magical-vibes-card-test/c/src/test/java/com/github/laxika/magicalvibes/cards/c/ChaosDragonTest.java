package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20ForEachPlayerAndRestrictSourceAttacksEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChaosDragon.class, GrizzlyBears.class, ChandraNalaar.class, InvasionOfZendikar.class})
class ChaosDragonTest extends BaseCardTest {

    private RollD20ForEachPlayerAndRestrictSourceAttacksEffectHandler effectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        effectHandler = GameTestEngineContext.get()
                .getBean(RollD20ForEachPlayerAndRestrictSourceAttacksEffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(effectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(effectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void highestOpponentRollPreventsChaosDragonFromAttackingThatPlayerOrTheirPlaneswalker() {
        setD20Rolls(Map.of(player1.getId(), 5, player2.getId(), 20));
        Permanent dragon = addCreatureReady(player1, new ChaosDragon());
        Permanent planeswalker = addPlaneswalker(player2);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThat(als.canAttackDefender(gd, dragon, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, dragon, planeswalker.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, bear, player2.getId())).isTrue();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).containsExactly(1);
    }

    @Test
    void sourceHighestRollLeavesOpponentAsAnAttackTargetAndChaosDragonMustAttack() {
        setD20Rolls(Map.of(player1.getId(), 20, player2.getId(), 5));
        Permanent dragon = addCreatureReady(player1, new ChaosDragon());

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThat(als.canAttackDefender(gd, dragon, player2.getId())).isTrue();
        declareAttackers(List.of(0));
        harness.assertLife(player2, 16);
    }

    @Test
    void attackRestrictionExpiresAtEndOfCombat() {
        setD20Rolls(Map.of(player1.getId(), 1, player2.getId(), 20));
        Permanent dragon = addCreatureReady(player1, new ChaosDragon());
        addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat();
        resolveAllTriggers();
        assertThat(als.canAttackDefender(gd, dragon, player2.getId())).isFalse();

        declareAttackers(List.of(1));

        assertThat(als.canAttackDefender(gd, dragon, player2.getId())).isTrue();
    }

    private void setD20Rolls(Map<UUID, Integer> results) {
        ReflectionTestUtils.setField(effectHandler, "d20RollService", new FixedD20RollService(results));
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        return planeswalker;
    }

    @Test
    void tiedHighestRollStillPreventsAttackingTheOpponent() {
        setD20Rolls(Map.of(player1.getId(), 20, player2.getId(), 20));
        Permanent dragon = addCreatureReady(player1, new ChaosDragon());
        Permanent planeswalker = addPlaneswalker(player2);

        advanceToBeginningOfCombat();
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);

        assertThat(als.canAttackDefender(gd, dragon, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, dragon, planeswalker.getId())).isFalse();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
        declareAttackers(List.of());
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotRollDiceDuringOpponentsCombat() {
        setD20Rolls(Map.of());
        addCreatureReady(player1, new ChaosDragon());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gameLogContains("rolls a d20 for Chaos Dragon")).isFalse();
    }

    @Test
    void mustAttackWhenThereIsALegalDefender() {
        setD20Rolls(Map.of(player1.getId(), 20, player2.getId(), 5));
        addCreatureReady(player1, new ChaosDragon());

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void highestOpponentRollDoesNotPreventAttackingABattleTheyControl() {
        UUID thirdPlayerId = UUID.randomUUID();
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Third player");
        gd.playerIdToName.put(thirdPlayerId, "Third player");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("third-player"), thirdPlayerId, "Third player");
        setD20Rolls(Map.of(player1.getId(), 5, player2.getId(), 20, thirdPlayerId, 10));
        Permanent dragon = addCreatureReady(player1, new ChaosDragon());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(thirdPlayerId);
        battle.setCounterCount(CounterType.DEFENSE, 3);

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThat(als.getValidAttackTargetIds(gd, player1.getId())).contains(battle.getId());
        assertThat(als.canAttackDefender(gd, dragon, battle.getId())).isTrue();
    }

    private static final class FixedD20RollService extends D20RollService {

        private final Map<UUID, Integer> results;

        private FixedD20RollService(Map<UUID, Integer> results) {
            this.results = results;
        }

        @Override
        public int roll(GameData gameData, UUID rollingPlayerId) {
            return results.get(rollingPlayerId);
        }
    }
}
