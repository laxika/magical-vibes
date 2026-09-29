package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20ForEachPlayerAndRestrictSourceAttacksEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosDragon.class, GrizzlyBears.class})
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
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
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
