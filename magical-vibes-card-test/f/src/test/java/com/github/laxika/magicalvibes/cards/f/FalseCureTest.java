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

    @Test
    @DisplayName("Life is gained before the life-loss trigger resolves")
    void lifeGainAndLifeLossResolveSeparately() {
        castFalseCure();
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new AncestorsProphet());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("False Cure triggers for every life gain during the turn")
    void triggersForRepeatedLifeGains() {
        harness.setLife(player1, 50);
        castFalseCure();

        gainTenLife(player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(40);

        gainTenLife(player1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(30);
    }

    @Test
    @DisplayName("Multiple resolved False Cures each cause life loss")
    void multipleCopiesTriggerIndependently() {
        harness.setLife(player1, 100);
        castFalseCure();
        castFalseCure();

        gainTenLife(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(70);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("False Cure still applies to life gained during the end step")
    void remainsActiveDuringEndStep() {
        castFalseCure();
        harness.passUntil(TurnStep.END_STEP);

        gainTenLife(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
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
