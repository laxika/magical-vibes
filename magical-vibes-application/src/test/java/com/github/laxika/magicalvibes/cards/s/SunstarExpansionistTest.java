package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunstarExpansionist.class, Forest.class})
class SunstarExpansionistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a Lander token when an opponent controls more lands")
    void etbCreatesLanderWhenOpponentControlsMoreLands() {
        harness.addToBattlefield(player2, new Forest());
        castExpansionist();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("ETB does not create a Lander token when land counts are equal")
    void etbDoesNotCreateLanderWhenLandCountsAreEqual() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castExpansionist();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("Landfall gives Sunstar Expansionist +1/+0 until end of turn")
    void landfallBoostsUntilEndOfTurn() {
        Permanent expansionist = harness.addToBattlefieldAndReturn(player1, new SunstarExpansionist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(expansionist.getEffectivePower()).isEqualTo(3);
        assertThat(expansionist.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(expansionist.getEffectivePower()).isEqualTo(2);
        assertThat(expansionist.getEffectiveToughness()).isEqualTo(3);
    }

    private void castExpansionist() {
        harness.setHand(player1, List.of(new SunstarExpansionist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
