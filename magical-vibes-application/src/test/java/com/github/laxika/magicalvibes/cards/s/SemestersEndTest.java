package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SemestersEndTest extends BaseCardTest {

    @Test
    @DisplayName("Returns selected creatures and planeswalkers with their appropriate counters")
    void returnsSelectedPermanentsWithAppropriateCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = new Permanent(new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);
        gd.playerBattlefields.get(player1.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Jace Beleren");

        advanceToEndStep();

        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        Permanent returnedPlaneswalker = findPermanent(player1, "Jace Beleren");
        assertThat(returnedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedPlaneswalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returnedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target only creatures and planeswalkers you control")
    void cannotTargetPermanentAnOpponentControls() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SemestersEnd()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker you control");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
