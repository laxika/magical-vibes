package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirriWeatherlightDuelist.class, GrizzlyBears.class})
class MirriWeatherlightDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Mirri limits the defending opponent to one blocker")
    void attackTriggerLimitsOpposingBlockers() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 distinct creature can block each combat");
    }

    @Test
    @DisplayName("A tapped Mirri limits attacks aimed at her controller")
    void tappedMirriLimitsAttacksAtController() {
        Permanent mirri = addCreatureReady(player1, new MirriWeatherlightDuelist());
        mirri.tap();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack each combat");
    }

    @Test
    @DisplayName("An untapped Mirri does not limit attacks aimed at her controller")
    void untappedMirriDoesNotLimitAttacksAtController() {
        addCreatureReady(player1, new MirriWeatherlightDuelist());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of(0, 1)))
                .doesNotThrowAnyException();
    }
}
