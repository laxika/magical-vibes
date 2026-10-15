package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlertHeedbonder.class, GrizzlyBears.class, SerraAngel.class})
class AlertHeedbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each vigilant creature you control")
    void gainsLifeForEachVigilantCreatureYouControl() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Counts vigilant creatures when the ability resolves")
    void countsVigilantCreaturesAtResolution() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Triggers only at the controller's end step")
    void triggersOnlyAtControllerEndStep() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Each Heedbonder triggers and counts both vigilant creatures")
    void multipleHeedbondersEachGainLife() {
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Tapped vigilant creatures still count")
    void tappedVigilantCreatureCounts() {
        var heedbonder = harness.addToBattlefieldAndReturn(player1, new AlertHeedbonder());
        heedbonder.tap();
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("A removed source does not count but its trigger still resolves")
    void removedSourceDoesNotCountAtResolution() {
        var removed = harness.addToBattlefieldAndReturn(player1, new AlertHeedbonder());
        harness.addToBattlefield(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        gd.playerGraveyards.get(player1.getId()).add(removed.getCard());

        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Gains no life when no vigilant creatures remain at resolution")
    void noVigilantCreaturesAtResolutionGainsNoLife() {
        var heedbonder = harness.addToBattlefieldAndReturn(player1, new AlertHeedbonder());
        harness.setLife(player1, 10);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(heedbonder);
        gd.playerGraveyards.get(player1.getId()).add(heedbonder.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }
    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
