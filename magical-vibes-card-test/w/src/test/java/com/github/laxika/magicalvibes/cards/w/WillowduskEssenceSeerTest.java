package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillowduskEssenceSeer.class, GrizzlyBears.class})
class WillowduskEssenceSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters equal to the greater of life gained or lost")
    void putsCountersEqualToGreaterLifeChange() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(willowdusk.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Uses life gained when it is greater than life lost")
    void usesLifeGainedWhenGreater() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target Willowdusk itself")
    void cannotTargetItself() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(willowdusk), null, willowdusk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(willowdusk), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void addsNoCountersWhenNoLifeWasGainedOrLost() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new WillowduskEssenceSeer());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(willowdusk.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equalLifeGainAndLossDoNotCancelEachOther() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new WillowduskEssenceSeer());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);
        gd.lifeLostThisTurn.put(player1.getId(), 4);
        gd.lifeGainedThisTurn.put(player2.getId(), 10);
        gd.lifeLostThisTurn.put(player2.getId(), 12);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void determinesLifeAmountAtResolution() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new WillowduskEssenceSeer());
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        gd.lifeGainedThisTurn.put(player1.getId(), 7);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new WillowduskEssenceSeer());
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, willowdusk));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        harness.assertInHand(player1, "Willowdusk, Essence Seer");
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new WillowduskEssenceSeer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(willowdusk), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        Permanent willowdusk = addCreatureReady(player1, new WillowduskEssenceSeer());
        Permanent target = addCreatureReady(player2, new WillowduskEssenceSeer());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(willowdusk), null, target.getId());
        willowdusk.untap();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(willowdusk), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
