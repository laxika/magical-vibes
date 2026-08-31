package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KavLandseeker.class)
class KavLandseekerTest extends BaseCardTest {

    @Test
    @DisplayName("When Kav Landseeker enters, it creates a Lander token")
    void createsLanderTokenOnEnter() {
        castKavLandseeker();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("The Lander is sacrificed at the end step on its controller's next turn")
    void sacrificesLanderAtEndStepOnControllersNextTurn() {
        castKavLandseeker();
        Permanent lander = findPermanents(player1, "Lander").getFirst();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).contains(lander);

        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).contains(lander);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(findPermanents(player1, "Lander")).contains(lander);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Lander")).isEmpty();
    }

    private void castKavLandseeker() {
        harness.setHand(player1, List.of(new KavLandseeker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
