package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientSilverDragon.class, Forest.class})
class AncientSilverDragonTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    @DisplayName("Combat damage draws cards equal to the d20 result")
    void combatDamageDrawsCardsEqualToRollResult() {
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        attackWithRoll(12);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 12);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage grants no maximum hand size for the rest of the game")
    void combatDamageGrantsNoMaximumHandSize() {
        attackWithRoll(1);

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(gd.playersWithNoMaximumHandSizeUntilNextTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Combat damage puts the entire ability on the stack as one trigger")
    void combatDamageCreatesOneTrigger() {
        addCreatureReady(player1, new AncientSilverDragon()).setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 12);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 9, 10, 19, 20})
    @DisplayName("Each d20 branch draws exactly the rolled number for the controller")
    void drawsExactlyTheRollAtBranchBoundaries(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        harness.setLibrary(player1, IntStream.range(0, 20).mapToObj(i -> new Forest()).toList());
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        addCreatureReady(player1, new AncientSilverDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + result);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(20 - result);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId()).doesNotContain(player2.getId());
    }

    @Test
    @DisplayName("Prevented combat damage does not trigger drawing or remove the hand limit")
    void preventedCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new AncientSilverDragon()).setAttacking(true);
        harness.forceActivePlayer(player1);
        gd.preventAllCombatDamage = true;
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("The hand-size grant persists after the Dragon leaves and a new turn begins")
    void handSizeGrantPersistsWithoutDragon() {
        attackWithRoll(1);
        gd.playerBattlefields.get(player1.getId()).clear();

        advanceToUpkeep(player1);

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(gd.playersWithNoMaximumHandSizeUntilNextTurn).doesNotContain(player1.getId());
    }

    private void attackWithRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new AncientSilverDragon());
        declareAttackers(List.of(0));
        resolveAllTriggers();
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
