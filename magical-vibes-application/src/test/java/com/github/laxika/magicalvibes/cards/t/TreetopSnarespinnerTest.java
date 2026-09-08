package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreetopSnarespinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability puts a +1/+1 counter on a creature you control")
    void putsCounterOnTargetCreatureYouControl() {
        Permanent snarespinner = addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSorcerySpeed();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(snarespinner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSorcerySpeed();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        addReadySnarespinner();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadySnarespinner() {
        Permanent snarespinner = new Permanent(new TreetopSnarespinner());
        snarespinner.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(snarespinner);
        return snarespinner;
    }

    private void prepareSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
