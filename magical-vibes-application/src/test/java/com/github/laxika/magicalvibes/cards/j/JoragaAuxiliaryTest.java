package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({JoragaAuxiliary.class, GrizzlyBears.class})
class JoragaAuxiliaryTest extends BaseCardTest {

    @Test
    @DisplayName("Support 2 puts a +1/+1 counter on each chosen creature")
    void supportPutsCountersOnTwoCreatures() {
        addReadyAuxiliary();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Support 2 may target only one creature")
    void supportMayTargetOneCreature() {
        addReadyAuxiliary();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent untouched = addCreatureReady(player1, new GrizzlyBears());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untouched.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Support 2 cannot target the source creature")
    void supportCannotTargetSource() {
        Permanent auxiliary = addReadyAuxiliary();
        addMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(auxiliary.getId())))
                .isInstanceOf(RuntimeException.class);
    }

    private Permanent addReadyAuxiliary() {
        Permanent auxiliary = new Permanent(new JoragaAuxiliary());
        auxiliary.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(auxiliary);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return auxiliary;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
