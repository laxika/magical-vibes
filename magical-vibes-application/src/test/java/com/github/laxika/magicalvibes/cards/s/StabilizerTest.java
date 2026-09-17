package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NobleTemplar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stabilizer.class, NobleTemplar.class, SparkSpray.class})
class StabilizerTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents its controller from cycling cards")
    void preventsControllerFromCycling() {
        harness.addToBattlefield(player1, new Stabilizer());
        harness.setHand(player1, List.of(new SparkSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Spark Spray");
    }

    @Test
    @DisplayName("Prevents opponents from cycling cards")
    void preventsOpponentFromCycling() {
        harness.addToBattlefield(player1, new Stabilizer());
        harness.setHand(player2, List.of(new SparkSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Spark Spray");
    }

    @Test
    @DisplayName("Prevents plainscycling as well as ordinary cycling")
    void preventsTypecycling() {
        harness.addToBattlefield(player1, new Stabilizer());
        harness.setHand(player1, List.of(new NobleTemplar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Noble Templar");
    }
}
