package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncestorsProphet;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalseCure.class, AncestorsProphet.class})
class FalseCureTest extends BaseCardTest {

    @Test
    @DisplayName("Each player loses twice the life they gain")
    void eachPlayerLosesTwiceTheLifeTheyGain() {
        castFalseCure();

        gainTenLife(player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gainTenLife(player2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Life gained before False Cure resolves is not affected")
    void doesNotAffectLifeGainedBeforeItResolves() {
        harness.castFromHand(player1, new FalseCure(), "{B}{B}");

        gainTenLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
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
        gainTenLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
    }

    private void castFalseCure() {
        harness.castFromHand(player1, new FalseCure(), "{B}{B}");
        harness.passBothPriorities();
    }

    private void gainTenLife(Player player) {
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player, new AncestorsProphet());
        }
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
