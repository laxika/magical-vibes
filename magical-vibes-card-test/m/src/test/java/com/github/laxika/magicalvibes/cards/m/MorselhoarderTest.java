package com.github.laxika.magicalvibes.cards.m;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Morselhoarder.class})
class MorselhoarderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (6/4 becomes 4/2)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Morselhoarder()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        assertThat(gd.stack).isEmpty();

        Permanent morselhoarder = findMorselhoarder(player1);

        assertThat(morselhoarder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(morselhoarder.getEffectivePower()).isEqualTo(4);
        assertThat(morselhoarder.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a -1/-1 counter adds one mana of the chosen color")
    void removeCounterAddsChosenColorMana() {
        Permanent morselhoarder = addReadyMorselhoarder(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(morselhoarder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability with no -1/-1 counters remaining")
    void cannotActivateWithoutCounters() {
        Permanent morselhoarder = addReadyMorselhoarder(player1);
        morselhoarder.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana ability works while tapped and summoning sick and can spend both counters")
    void manaAbilityWorksWhileTappedAndSummoningSick() {
        Permanent morselhoarder = addReadyMorselhoarder(player1);
        morselhoarder.setSummoningSick(true);
        morselhoarder.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(morselhoarder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player1, 0, null, null);
        assertThat(morselhoarder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(morselhoarder.isTapped()).isTrue();
        assertThat(morselhoarder.getEffectivePower()).isEqualTo(6);
        assertThat(morselhoarder.getEffectiveToughness()).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.EnumSource(value = ManaColor.class,
            names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canProduceEachManaColor(ManaColor color) {
        Permanent morselhoarder = addReadyMorselhoarder(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(morselhoarder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyMorselhoarder(Player player) {
        Permanent perm = addCreatureReady(player, new Morselhoarder());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return perm;
    }

    private Permanent findMorselhoarder(Player player) {
        return findPermanent(player, "Morselhoarder");
    }
}
