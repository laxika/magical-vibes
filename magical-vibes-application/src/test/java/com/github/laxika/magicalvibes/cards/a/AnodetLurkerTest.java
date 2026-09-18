package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AnodetLurker.class)
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
}
