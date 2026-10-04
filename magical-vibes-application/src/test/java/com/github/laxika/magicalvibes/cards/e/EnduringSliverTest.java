package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.IcehideGolem;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnduringSliver.class, UniversalAutomaton.class, IcehideGolem.class})
class EnduringSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Enduring Sliver can outlast itself at sorcery speed")
    void outlastsItself() {
        Permanent enduringSliver = addCreatureReady(player1, new EnduringSliver());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(enduringSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enduringSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other Sliver creatures you control gain outlast")
    void grantsOutlastToOtherSliversYouControl() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent ownSliver = addCreatureReady(player1, new UniversalAutomaton());
        Permanent opposingSliver = addCreatureReady(player2, new UniversalAutomaton());
        Permanent nonSliver = addCreatureReady(player1, new IcehideGolem());

        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonSliver)).isEmpty();

        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownSliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastIsSorcerySpeed() {
        addCreatureReady(player1, new EnduringSliver());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void outlastPaysItsTapCostBeforeTheCounterIsAdded() {
        Permanent sliver = addCreatureReady(player1, new EnduringSliver());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sliver.isTapped()).isTrue();
        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        prepareForSorceryAction();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void grantedOutlastRequiresTwoMana() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent sliver = addCreatureReady(player1, new UniversalAutomaton());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sliver.isTapped()).isFalse();
        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void grantedOutlastCannotBeUsedWhileSummoningSick() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent sliver = addCreatureReady(player1, new UniversalAutomaton());
        sliver.setSummoningSick(true);
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    void grantedOutlastCannotBeUsedDuringOpponentsMainPhase() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent sliver = addCreatureReady(player1, new UniversalAutomaton());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    void grantedOutlastCannotBeUsedWithAnAbilityOnTheStack() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent sliver = addCreatureReady(player1, new UniversalAutomaton());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sliver.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void activatedOutlastResolvesAfterTheGrantingSliverLeaves() {
        Permanent source = addCreatureReady(player1, new EnduringSliver());
        Permanent sliver = addCreatureReady(player1, new UniversalAutomaton());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(sliver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sliver.isTapped()).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, sliver)).isEmpty();
    }

    @Test
    void anotherEnduringSliverCanUseTheGrantedOutlast() {
        Permanent source = addCreatureReady(player1, new EnduringSliver());
        Permanent recipient = addCreatureReady(player1, new EnduringSliver());
        prepareForSorceryAction();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recipient.isTapped()).isTrue();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.isTapped()).isFalse();
    }

    private void prepareForSorceryAction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
