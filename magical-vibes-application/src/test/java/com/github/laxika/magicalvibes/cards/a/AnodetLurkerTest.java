package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnodetLurker.class})
class AnodetLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("When Anodet Lurker dies, its controller gains 3 life")
    void gainsLifeWhenItDies() {
        harness.setLife(player1, 10);
        Permanent lurker = harness.addToBattlefieldAndReturn(player1, new AnodetLurker());
        lurker.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Anodet Lurker");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A surviving Anodet Lurker does not trigger its death ability")
    void doesNotTriggerWhileItSurvives() {
        harness.setLife(player1, 10);
        Permanent lurker = harness.addToBattlefieldAndReturn(player1, new AnodetLurker());
        lurker.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Anodet Lurker");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("An opponent's dying Anodet Lurker gains life only for that opponent on resolution")
    void opponentGainsLifeOnlyWhenTheirTriggerResolves() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        Permanent lurker = harness.addToBattlefieldAndReturn(player2, new AnodetLurker());
        lurker.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Anodet Lurker");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 7);

        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneously dying Anodet Lurkers each gain life for their own controller")
    void simultaneousDeathsGainLifeForEachController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 7);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AnodetLurker());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AnodetLurker());
        first.setMarkedDamage(3);
        second.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Anodet Lurker");
        harness.assertInGraveyard(player2, "Anodet Lurker");
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 7);

        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 10);
        assertThat(gd.stack).isEmpty();
    }
}
