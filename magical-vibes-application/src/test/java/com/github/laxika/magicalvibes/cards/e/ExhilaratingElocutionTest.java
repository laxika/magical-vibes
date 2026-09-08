package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

class ExhilaratingElocutionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two counters on the target and boosts other creatures you control")
    void putsCountersOnTargetAndBoostsOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectivePower()).isEqualTo(5);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The temporary boost wears off while counters remain")
    void temporaryBoostWearsOffButCountersRemain() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectivePower()).isEqualTo(4);
        assertThat(other.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ExhilaratingElocution()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
