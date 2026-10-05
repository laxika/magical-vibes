package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JeskaiDevotee;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoxodonBattlePriest.class, JeskaiDevotee.class, Island.class})
class LoxodonBattlePriestTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on another creature you control at the beginning of combat")
    void putsCounterAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new LoxodonBattlePriest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new LoxodonBattlePriest());
        harness.addToBattlefield(player1, new JeskaiDevotee());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, priest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentCombat() {
        harness.addToBattlefield(player1, new LoxodonBattlePriest());
        harness.addToBattlefield(player1, new JeskaiDevotee());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsCreatureOrOwnLand() {
        harness.addToBattlefield(player1, new LoxodonBattlePriest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new JeskaiDevotee());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void noLegalTargetDoesNotPutAbilityOnStack() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new LoxodonBattlePriest());
        harness.addToBattlefield(player2, new JeskaiDevotee());
        harness.addToBattlefield(player1, new Island());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void abilityStillResolvesAfterPriestLeavesBattlefield() {
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new LoxodonBattlePriest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(priest);
        gd.playerGraveyards.get(player1.getId()).add(priest.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void targetBecomingOpponentControlledGetsNoCounter() {
        harness.addToBattlefield(player1, new LoxodonBattlePriest());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JeskaiDevotee());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
