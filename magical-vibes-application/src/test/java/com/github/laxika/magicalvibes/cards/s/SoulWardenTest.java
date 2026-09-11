package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulWarden.class, RagingGoblin.class})
class SoulWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature enters under its controller's control")
    void gainsLifeWhenAnotherCreatureEntersUnderItsControllersControl() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Its controller gains life when an opponent's creature enters")
    void itsControllerGainsLifeWhenOpponentsCreatureEnters() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new RagingGoblin(), "{R}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Soul Warden triggers for the same creature entering")
    void eachSoulWardenTriggersIndependently() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RagingGoblin(), "{R}");
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not trigger when Soul Warden itself enters")
    void doesNotTriggerForItself() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new SoulWarden(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
