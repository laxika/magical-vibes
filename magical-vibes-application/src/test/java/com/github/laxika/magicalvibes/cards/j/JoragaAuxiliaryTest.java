package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.m.MakindiAeronaut;
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

@CardUsed({JoragaAuxiliary.class, MakindiAeronaut.class})
class JoragaAuxiliaryTest extends BaseCardTest {

    @Test
    @DisplayName("Support 2 puts a +1/+1 counter on each chosen creature")
    void supportPutsCountersOnTwoCreatures() {
        addReadyAuxiliary();
        Permanent ownCreature = addCreatureReady(player1, new MakindiAeronaut());
        Permanent opposingCreature = addCreatureReady(player2, new MakindiAeronaut());
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
        Permanent creature = addCreatureReady(player1, new MakindiAeronaut());
        Permanent untouched = addCreatureReady(player1, new MakindiAeronaut());
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

    @Test
    @DisplayName("Support 2 may choose no targets")
    void supportMayChooseNoTargets() {
        Permanent auxiliary = addReadyAuxiliary();
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(auxiliary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Support cannot target the same creature twice")
    void supportRejectsDuplicateTargets() {
        addReadyAuxiliary();
        Permanent creature = addCreatureReady(player1, new MakindiAeronaut());
        addMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Support cannot choose more than two creatures")
    void supportRejectsThreeTargets() {
        addReadyAuxiliary();
        Permanent first = addCreatureReady(player1, new MakindiAeronaut());
        Permanent second = addCreatureReady(player1, new MakindiAeronaut());
        Permanent third = addCreatureReady(player2, new MakindiAeronaut());
        addMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Support resolves for the remaining target when another leaves")
    void supportResolvesForRemainingTarget() {
        addReadyAuxiliary();
        Permanent first = addCreatureReady(player1, new MakindiAeronaut());
        Permanent second = addCreatureReady(player2, new MakindiAeronaut());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Support resolves after its source leaves the battlefield")
    void supportResolvesWithoutSource() {
        Permanent auxiliary = addReadyAuxiliary();
        Permanent creature = addCreatureReady(player1, new MakindiAeronaut());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(auxiliary);
        gd.playerGraveyards.get(player1.getId()).add(auxiliary.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Support needs neither an untapped source nor haste")
    void supportWorksWhileTappedAndSummoningSick() {
        Permanent auxiliary = addReadyAuxiliary();
        auxiliary.tap();
        auxiliary.setSummoningSick(true);
        Permanent creature = addCreatureReady(player1, new MakindiAeronaut());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(auxiliary.isTapped()).isTrue();
    }

    private Permanent addReadyAuxiliary() {
        Permanent auxiliary = addCreatureReady(player1, new JoragaAuxiliary());
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
