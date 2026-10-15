package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Haruspex.class, GrizzlyBears.class, Shock.class})
class HaruspexTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature dies")
    void anotherCreatureDiesAddsCounter() {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes X +1/+1 counters and adds X mana of one chosen color")
    void removesCountersForAnyColorMana() {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        haruspex.setSummoningSick(false);
        haruspex.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.XValueChoice xChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(xChoice).isNotNull();
        assertThat(xChoice.maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(haruspex.isTapped()).isTrue();
    }

    @Test
    void ownCreatureDeathTriggersButSelfDeathDoesNot() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, dying.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(watcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dying.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dying);
    }

    @Test
    void zeroCountersCanBeRemovedForZeroMana() {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        haruspex.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null);

        assertThat(haruspex.isTapped()).isTrue();
        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    void cannotRemoveMoreCountersThanAvailable() {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        haruspex.setSummoningSick(false);
        haruspex.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(haruspex.isTapped()).isFalse();
    }

    @Test
    void summoningSicknessPreventsManaAbility() {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        haruspex.setSummoningSick(true);
        haruspex.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(haruspex.isTapped()).isFalse();
    }

    @Test
    void tappedSourceCannotActivateManaAbility() {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        haruspex.setSummoningSick(false);
        haruspex.tap();
        haruspex.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void allCountersProduceManaOfExactlyOneChosenColor(ManaColor chosenColor) {
        Permanent haruspex = harness.addToBattlefieldAndReturn(player1, new Haruspex());
        haruspex.setSummoningSick(false);
        haruspex.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.handleListChoice(player1, chosenColor.name());

        assertThat(haruspex.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(haruspex.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 3 : 0);
        }
    }
}
