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
        harness.addMana(player2, ManaColor.COLORLESS, 2);

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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void castAngel() {
        harness.setHand(player1, List.of(new AngelOfEternalDawn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
