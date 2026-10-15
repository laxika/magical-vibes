package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HussarPatrol.class, DrudgeBeetle.class})
class HussarPatrolTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Can cast during opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HussarPatrol()));
        addCastingMana();

        harness.passPriority(player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hussar Patrol");
    }

    @Test
    @DisplayName("Can cast during combat step thanks to Flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HussarPatrol()));
        addCastingMana();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hussar Patrol");
    }

    @Test
    @DisplayName("Non-flash creature cannot be cast during combat step")
    void nonFlashCreatureCannotCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Hussar Patrol onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new HussarPatrol()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Hussar Patrol");
    }

    @Test
    @DisplayName("Vigilance: attacking does not tap Hussar Patrol")
    void vigilanceDoesNotTapWhenAttacking() {
        Permanent patrol = addCreatureReady(player1, new HussarPatrol());

        declareAttackers(List.of(0));

        assertThat(patrol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature without Vigilance taps when attacking")
    void nonVigilanceCreatureTapsWhenAttacking() {
        Permanent beetle = addCreatureReady(player1, new DrudgeBeetle());

        declareAttackers(List.of(0));

        assertThat(beetle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flash permits casting in response to another creature spell")
    void canCastWithAnotherSpellOnStack() {
        harness.setHand(player1, List.of(new HussarPatrol(), new HussarPatrol()));
        addCastingMana();
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Hussar Patrol")).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Hussar Patrol")).isEqualTo(2);
    }
}
