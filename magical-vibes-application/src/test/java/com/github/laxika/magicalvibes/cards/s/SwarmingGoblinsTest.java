package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({SwarmingGoblins.class, PowerWordKill.class})
class SwarmingGoblinsTest extends BaseCardTest {

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
    @DisplayName("A result from 1 through 9 creates one Goblin")
    void lowResultCreatesOneGoblin() {
        setRoll(9);

        castSwarmingGoblins();

        assertGoblins(1);
    }

    @Test
    @DisplayName("A result from 10 through 19 creates two Goblins")
    void middleResultCreatesTwoGoblins() {
        setRoll(19);

        castSwarmingGoblins();

        assertGoblins(2);
    }

    @Test
    @DisplayName("A result of 20 creates three Goblins")
    void maximumResultCreatesThreeGoblins() {
        setRoll(20);

        castSwarmingGoblins();

        assertGoblins(3);
    }

    private void castSwarmingGoblins() {
        harness.castFromHand(player1, new SwarmingGoblins(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A result of 1 creates one Goblin")
    void minimumResultCreatesOneGoblin() {
        setRoll(1);

        castSwarmingGoblins();

        assertGoblins(1);
    }

    @Test
    @DisplayName("A result of 10 creates two Goblins")
    void firstMiddleResultCreatesTwoGoblins() {
        setRoll(10);

        castSwarmingGoblins();

        assertGoblins(2);
    }

    @Test
    @CardUsed({SwarmingGoblins.class, PowerWordKill.class})
    @DisplayName("The enter trigger creates tokens even after Swarming Goblins is destroyed")
    void triggerResolvesAfterSourceLeaves() {
        setRoll(20);
        harness.castFromHand(player1, new SwarmingGoblins(), "{4}{R}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        Permanent source = findPermanents(player1, "Swarming Goblins").getFirst();

        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Swarming Goblins");
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
        resolveAllTriggers();

        assertGoblins(3);
        assertThat(findPermanents(player2, "Goblin")).isEmpty();
    }

    private void assertGoblins(int expectedCount) {
        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(expectedCount);
        assertThat(goblins).allSatisfy(goblin -> {
            assertThat(goblin.getCard().isToken()).isTrue();
            assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(goblin.getCard().getSubtypes()).containsExactly(CardSubtype.GOBLIN);
            assertThat(goblin.getEffectivePower()).isEqualTo(1);
            assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
        });
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
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
