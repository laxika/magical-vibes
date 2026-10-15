package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Snarespinner;
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

@CardUsed({MerfolkSkydiver.class, Snarespinner.class})
class MerfolkSkydiverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new Snarespinner());

        harness.setHand(player1, List.of(new MerfolkSkydiver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, recipient.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Snarespinner());

        harness.setHand(player1, List.of(new MerfolkSkydiver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Activated ability proliferates")
    void activatedAbilityProliferates() {
        Permanent skydiver = harness.addToBattlefieldAndReturn(player1, new MerfolkSkydiver());
        skydiver.setSummoningSick(false);

        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        recipient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(recipient.getId()));

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can put its counter on the Skydiver itself")
    void etbCanTargetItself() {
        Permanent skydiver = harness.enterBattlefieldAndReturn(player1, new MerfolkSkydiver());
        harness.handlePermanentChosen(player1, skydiver.getId());
        harness.passBothPriorities();

        assertThat(skydiver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate adds each existing counter kind to selected permanents and players")
    void proliferatesOpposingPermanentAndPlayer() {
        Permanent skydiver = harness.addToBattlefieldAndReturn(player1, new MerfolkSkydiver());
        skydiver.tap();
        Permanent selected = harness.addToBattlefieldAndReturn(player2, new Snarespinner());
        selected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        selected.setCounterCount(CounterType.STUN, 1);
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        unselected.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        activateProliferate();
        harness.handleMultiplePermanentsChosen(player1, List.of(selected.getId(), player2.getId()));

        assertThat(selected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(selected.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(selected.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(skydiver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Proliferate may choose no permanents or players")
    void proliferateCanChooseNothing() {
        Permanent skydiver = harness.addToBattlefieldAndReturn(player1, new MerfolkSkydiver());
        skydiver.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activateProliferate();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(skydiver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Proliferate resolves when there are no counters")
    void proliferateWithNoCounters() {
        Permanent skydiver = harness.addToBattlefieldAndReturn(player1, new MerfolkSkydiver());

        activateProliferate();

        assertThat(skydiver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void activateProliferate() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
