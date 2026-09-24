package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirriWeatherlightDuelist.class, GrizzlyBears.class})
class MirriWeatherlightDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Mirri limits attacks against her controller to one creature")
    void tappedMirriLimitsAttacksAgainstController() {
        Permanent mirri = addCreatureReady(player2, new MirriWeatherlightDuelist());
        mirri.tap();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("An untapped Mirri does not limit attacks against her controller")
    void untappedMirriDoesNotLimitAttacksAgainstController() {
        addCreatureReady(player2, new MirriWeatherlightDuelist());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("When Mirri attacks, each opponent can block with only one creature this combat")
    void attackTriggerLimitsBlockers() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }
}
