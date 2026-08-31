package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({EvolutionVat.class, GrizzlyBears.class, Forest.class})
class EvolutionVatTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the target creature, adds a +1/+1 counter, and grants the temporary ability")
    void tapsCountersAndGrantsAbility() {
        Permanent vat = addReadyVat(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(vat.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The granted ability doubles the target creature's +1/+1 counters")
    void grantedAbilityDoublesCounters() {
        addReadyVat(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("The granted ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        addReadyVat(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Only creature permanents can be targeted")
    void cannotTargetNonCreaturePermanent() {
        addReadyVat(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVat(Player player) {
        Permanent vat = new Permanent(new EvolutionVat());
        vat.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vat);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return vat;
    }
}
