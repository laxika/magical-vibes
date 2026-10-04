package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GracefulCat.class})
class GracefulCatTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(1);
        assertThat(cat.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cat.getPowerModifier()).isEqualTo(0);
        assertThat(cat.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when it does not attack")
    void noBoostWithoutAttacking() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(0);
        assertThat(cat.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost waits for its triggered ability to resolve")
    void boostWaitsForResolution() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(cat.getPowerModifier()).isZero();
        assertThat(cat.getToughnessModifier()).isZero();

        resolveAllTriggers();

        assertThat(cat.getPowerModifier()).isEqualTo(1);
        assertThat(cat.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the attacking Cat receives its boost")
    void onlyAttackingCatIsBoosted() {
        Permanent attacker = addCreatureReady(player1, new GracefulCat());
        Permanent otherCat = addCreatureReady(player1, new GracefulCat());
        Permanent opposingCat = addCreatureReady(player2, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(otherCat.getPowerModifier()).isZero();
        assertThat(otherCat.getToughnessModifier()).isZero();
        assertThat(opposingCat.getPowerModifier()).isZero();
        assertThat(opposingCat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking Cat receives exactly its own boost")
    void multipleAttackingCatsBoostIndependently() {
        Permanent first = addCreatureReady(player1, new GracefulCat());
        Permanent second = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
    }
}
