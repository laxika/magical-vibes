package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngelOfEternalDawn.class, GrizzlyBears.class})
class AngelOfEternalDawnTest extends BaseCardTest {

    @Test
    void becomesDayWhenItEntersEvenIfItWasNight() {
        gd.dayNight = DayNight.NIGHT;
        castAngel();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void opponentsCannotCastSpellsAboveTheirTurnsTaken() {
        gd.turnsTakenByPlayer.put(player2.getId(), 1);
        harness.addToBattlefield(player1, new AngelOfEternalDawn());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opponentsMayCastSpellsWithinTheirTurnsTaken() {
        gd.turnsTakenByPlayer.put(player2.getId(), 2);
        harness.addToBattlefield(player1, new AngelOfEternalDawn());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void becomesDayWhenNoDesignationHasBeenEstablished() {
        gd.dayNight = DayNight.NEITHER;

        castAngel();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void cannotBecomeNightOnControllersTurnAfterNoSpellsWereCast() {
        harness.addToBattlefield(player1, new AngelOfEternalDawn());
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.put(player1.getId(), 0);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void cannotBecomeNightOnOpponentsTurnAfterNoSpellsWereCast() {
        harness.addToBattlefield(player1, new AngelOfEternalDawn());
        gd.dayNight = DayNight.DAY;
        gd.spellsCastLastTurn.put(player2.getId(), 0);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void controllerMayCastSpellsAboveTheirTurnsBegun() {
        gd.turnsTakenByPlayer.put(player1.getId(), 1);
        harness.addToBattlefield(player1, new AngelOfEternalDawn());

        harness.castFromHand(player1, new AngelOfEternalDawn(), "{2}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void restrictionUsesOpponentsTurnsRatherThanAngelsControllersTurns() {
        gd.turnsTakenByPlayer.put(player1.getId(), 1);
        gd.turnsTakenByPlayer.put(player2.getId(), 3);
        harness.addToBattlefield(player1, new AngelOfEternalDawn());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AngelOfEternalDawn(), "{2}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    private void castAngel() {
        harness.castFromHand(player1, new AngelOfEternalDawn(), "{2}{W}");
        resolveAllTriggers();
    }
}
