package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HyenaPack;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChannelerInitiate.class, HyenaPack.class})
class ChannelerInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts three -1/-1 counters on a creature you control")
    void etbPutsThreeCountersOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new HyenaPack());

        harness.setHand(player1, List.of(new ChannelerInitiate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, elemental.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Hyena Pack (3/4) with three -1/-1 counters → 0/1.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(elemental.getEffectivePower()).isZero();
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new HyenaPack()).getId();

        harness.setHand(player1, List.of(new ChannelerInitiate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing a -1/-1 counter taps the creature and adds one mana of the chosen color")
    void removeCounterAddsChosenColorMana() {
        Permanent initiate = addReadyInitiate(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(initiate.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(initiate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the mana ability with no -1/-1 counters remaining")
    void cannotActivateWithoutCounters() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfAfterEnteringAnEmptyBattlefield() {
        harness.setHand(player1, List.of(new ChannelerInitiate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID initiateId = harness.getPermanentId(player1, "Channeler Initiate");
        harness.handlePermanentChosen(player1, initiateId);
        harness.passBothPriorities();

        Permanent initiate = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(initiate.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(initiate.getEffectivePower()).isZero();
        assertThat(initiate.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void countersCanKillTheTargetWithoutBeingPlacedOnTheSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HyenaPack());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new ChannelerInitiate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hyena Pack");
        harness.assertNotOnBattlefield(player1, "Hyena Pack");
        Permanent initiate = findPermanent(player1, "Channeler Initiate");
        assertThat(initiate.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(initiate.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(initiate.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(initiate.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    void cannotPayWithPlusOneCountersOrAnotherCreaturesCounters() {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ChannelerInitiate());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(initiate.isTapped()).isFalse();
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void lastCounterProducesAnyColorWithoutUsingTheStack(ManaColor color) {
        Permanent initiate = addReadyInitiate(player1);
        initiate.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        int before = gd.playerManaPools.get(player1.getId()).get(color);

        harness.activateAbility(player1, 0, null, null);

        assertThat(initiate.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(initiate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(before + 1);
        assertThat(gd.stack).isEmpty();

        initiate.setTapped(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(initiate.isTapped()).isFalse();
    }

    private Permanent addReadyInitiate(Player player) {
        Permanent perm = addCreatureReady(player, new ChannelerInitiate());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        return perm;
    }
}
