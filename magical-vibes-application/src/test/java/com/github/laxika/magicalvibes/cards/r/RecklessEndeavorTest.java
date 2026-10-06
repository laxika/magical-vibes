package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BarbarianClass;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.D12RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RecklessEndeavorEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessEndeavor.class, AirElemental.class, BarbarianClass.class})
class RecklessEndeavorTest extends BaseCardTest {

    private RecklessEndeavorEffectHandler effectHandler;
    private D12RollService originalD12RollService;

    @BeforeEach
    void captureD12RollService() {
        effectHandler = GameTestEngineContext.get().getBean(RecklessEndeavorEffectHandler.class);
        originalD12RollService = (D12RollService) ReflectionTestUtils.getField(effectHandler, "d12RollService");
    }

    @AfterEach
    void restoreD12RollService() {
        ReflectionTestUtils.setField(effectHandler, "d12RollService", originalD12RollService);
    }

    @Test
    void choosingFirstRollDealsThatMuchAndCreatesTheOtherNumberOfTreasures() {
        castWithRolls(3, 8);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("3", "8");

        harness.handleListChoice(player1, "3");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(8);
        assertThat(countPermanents(player2, "Air Elemental")).isEqualTo(1);
    }

    @Test
    void choosingSecondRollCreatesTheOtherNumberOfTreasures() {
        castWithRolls(3, 8);

        harness.handleListChoice(player1, "8");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(countPermanents(player2, "Air Elemental")).isZero();
    }

    private void castWithRolls(int first, int second) {
        ReflectionTestUtils.setField(effectHandler, "d12RollService", new FixedD12RollService(first, second));
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new RecklessEndeavor(), "{5}{R}{R}");
        harness.passBothPriorities();
    }

    @Test
    void equalRollsResolveWithoutAChoice() {
        castWithRolls(4, 4);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Reckless Endeavor");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
    }

    @Test
    void damageHitsCreaturesControlledByBothPlayersButNotPlayers() {
        harness.addToBattlefield(player1, new AirElemental());
        castWithRolls(4, 12);

        harness.handleListChoice(player1, "4");

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(12);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void createsTreasuresEvenWhenThereAreNoCreatures() {
        ReflectionTestUtils.setField(effectHandler, "d12RollService", new FixedD12RollService(1, 12));
        harness.castFromHand(player1, new RecklessEndeavor(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "12");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Reckless Endeavor");
    }

    @Test
    void barbarianClassAddsOneDieToTheWholeRollAndIgnoresOnlyTheLowest() {
        harness.addToBattlefield(player1, new BarbarianClass());
        ReflectionTestUtils.setField(effectHandler, "d12RollService", new FixedD12RollService(12, 8, 1, 1));
        harness.castFromHand(player1, new RecklessEndeavor(), "{5}{R}{R}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("12", "8");

        harness.handleListChoice(player1, "12");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(8);
        harness.assertOnBattlefield(player1, "Barbarian Class");
    }

    private static final class FixedD12RollService extends D12RollService {

        private final int[] results;
        private int index;

        private FixedD12RollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll() {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
