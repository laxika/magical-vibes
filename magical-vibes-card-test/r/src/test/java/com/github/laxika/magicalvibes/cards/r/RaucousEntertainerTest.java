package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BristlepackSentry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaucousEntertainer.class, BristlepackSentry.class})
class RaucousEntertainerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on creatures you control that entered this turn")
    void putsCountersOnCreaturesThatEnteredThisTurn() {
        Permanent entertainer = harness.enterBattlefieldAndReturn(player1, new RaucousEntertainer());
        entertainer.setSummoningSick(false);
        Permanent enteredCreature = harness.enterBattlefieldAndReturn(player1, new BristlepackSentry());
        Permanent oldCreature = harness.addToBattlefieldAndReturn(player1, new BristlepackSentry());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new BristlepackSentry());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(entertainer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(enteredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(oldCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Ability includes creatures that enter after activation and survives its source leaving")
    void checksCreaturesAtResolutionWithoutSource() {
        Permanent entertainer = addCreatureReady(player1, new RaucousEntertainer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(entertainer.isTapped()).isTrue();

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new BristlepackSentry());
        gd.playerBattlefields.get(player1.getId()).remove(entertainer);
        gd.playerGraveyards.get(player1.getId()).add(entertainer.getCard());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature that entered under an opponent's control qualifies after changing control")
    void includesCreatureThatEnteredUnderAnotherController() {
        Permanent entertainer = addCreatureReady(player1, new RaucousEntertainer());
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new BristlepackSentry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(entertainer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick entertainer cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent entertainer = harness.enterBattlefieldAndReturn(player1, new RaucousEntertainer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(entertainer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires one mana even when there are no qualifying creatures")
    void requiresManaAndResolvesWithNoQualifyingCreatures() {
        Permanent entertainer = addCreatureReady(player1, new RaucousEntertainer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(entertainer.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(entertainer.isTapped()).isTrue();
        assertThat(entertainer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
