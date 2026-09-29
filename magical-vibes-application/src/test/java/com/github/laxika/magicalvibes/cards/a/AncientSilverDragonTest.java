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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

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
