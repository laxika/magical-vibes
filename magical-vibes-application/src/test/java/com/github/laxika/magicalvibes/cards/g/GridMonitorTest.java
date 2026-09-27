package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GridMonitor.class, GoblinStriker.class, Shatter.class})
class GridMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller cannot cast creature spells while Grid Monitor is on the battlefield")
    void controllerCannotCastCreatureSpells() {
        harness.addToBattlefield(player1, new GridMonitor());
        harness.setHand(player1, List.of(new GoblinStriker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Controller can still cast noncreature spells while Grid Monitor is on the battlefield")
    void controllerCanCastNonCreatureSpells() {
        harness.addToBattlefield(player1, new GridMonitor());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grid Monitor"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Opponent can still cast creature spells while controller has Grid Monitor")
    void opponentCanStillCastCreatureSpells() {
        harness.addToBattlefield(player1, new GridMonitor());
        harness.setHand(player2, List.of(new GoblinStriker()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
