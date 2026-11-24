package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BarbarianClass;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollTwoDiceChooseOneEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessEndeavor.class, AirElemental.class, BarbarianClass.class})
class RecklessEndeavorTest extends BaseCardTest {

    private RollTwoDiceChooseOneEffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(RollTwoDiceChooseOneEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    @DisplayName("Choosing first roll deals that much and creates the other number of treasures")
    void choosingFirstRollDealsThatMuchAndCreatesTheOtherNumberOfTreasures() {
        castWithRolls(3, 8);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("3", "8");

        harness.handleListChoice(player1, "3");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(8);
        assertThat(countPermanents(player2, "Air Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing second roll creates the other number of treasures")
    void choosingSecondRollCreatesTheOtherNumberOfTreasures() {
        castWithRolls(3, 8);

        harness.handleListChoice(player1, "8");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        assertThat(countPermanents(player2, "Air Elemental")).isZero();
    }

    private void castWithRolls(int first, int second) {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(first, second));
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new RecklessEndeavor(), "{5}{R}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Equal rolls resolve without a choice")
    void equalRollsResolveWithoutAChoice() {
        castWithRolls(4, 4);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Reckless Endeavor");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
    }

    @Test
    @DisplayName("Damage hits creatures controlled by both players but not players")
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
    @DisplayName("Creates treasures even when there are no creatures")
    void createsTreasuresEvenWhenThereAreNoCreatures() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1, 12));
        harness.castFromHand(player1, new RecklessEndeavor(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.handleListChoice(player1, "12");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Reckless Endeavor");
    }

    @Test
    @DisplayName("Barbarian class adds one die to the whole roll and ignores only the lowest")
    void barbarianClassAddsOneDieToTheWholeRollAndIgnoresOnlyTheLowest() {
        harness.addToBattlefield(player1, new BarbarianClass());
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(12, 8, 1, 1));
        harness.castFromHand(player1, new RecklessEndeavor(), "{5}{R}{R}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder("12", "8");

        harness.handleListChoice(player1, "12");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(8);
        harness.assertOnBattlefield(player1, "Barbarian Class");
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int[] results;
        private int index;

        private FixedDiceRollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll(int sides) {
            return results[Math.min(index++, results.length - 1)];
        }
    }
}
