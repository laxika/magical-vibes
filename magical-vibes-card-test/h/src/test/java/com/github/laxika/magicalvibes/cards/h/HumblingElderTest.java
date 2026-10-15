package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HumblingElder.class, GrizzlyBears.class})
class HumblingElderTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast during the opponent's turn because of flash")
    void canBeCastDuringOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);

        harness.castCreature(player1, 0, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("ETB gives an opponent's creature -2/-0 until end of turn")
    void etbWeakensOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Can enter when the opponent controls no creatures")
    void canEnterWithoutLegalTargets() {
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Humbling Elder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The debuff can reduce power below zero and only affects the target")
    void canReducePowerBelowZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumblingElder());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HumblingElder());
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.getEffectivePower()).isEqualTo(1);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB ability resolves even after Humbling Elder leaves")
    void triggerResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HumblingElder());
        harness.setHand(player1, List.of(new HumblingElder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(-1);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
