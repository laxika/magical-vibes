package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalseCure.class, AngelOfMercy.class})
class FalseCureTest extends BaseCardTest {

    @Test
    @DisplayName("Each player loses twice the life they gain")
    void eachPlayerLosesTwiceTheLifeTheyGain() {
        castFalseCure();

        gainThreeLife(player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainThreeLife(player2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The delayed trigger expires at the end of the turn")
    void delayedTriggerExpiresAtEndOfTurn() {
        castFalseCure();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainThreeLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    private void castFalseCure() {
        harness.setHand(player1, List.of(new FalseCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void gainThreeLife(Player player) {
        harness.setHand(player, List.of(new AngelOfMercy()));
        harness.addMana(player, ManaColor.WHITE, 5);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
