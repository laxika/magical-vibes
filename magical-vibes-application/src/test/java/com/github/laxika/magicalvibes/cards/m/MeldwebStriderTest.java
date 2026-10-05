package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FurnaceStrider;
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

@CardUsed({MeldwebStrider.class, FurnaceStrider.class})
class MeldwebStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an oil counter")
    void entersWithOilCounter() {
        harness.setHand(player1, List.of(new MeldwebStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent strider = findPermanent(player1, "Meldweb Strider");
        assertThat(strider.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, strider)).isFalse();
    }

    @Test
    @DisplayName("Removing an oil counter animates it until end of turn")
    void removingOilCounterAnimatesIt() {
        Permanent strider = addReadyStrider(1);

        harness.activateAbility(player1, indexOf(strider), 0, null, null);
        harness.passBothPriorities();

        assertThat(strider.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.isCreature(gd, strider)).isTrue();
        assertThat(gqs.isArtifact(strider)).isTrue();
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, strider)).isFalse();
    }

    @Test
    @DisplayName("Cannot remove an oil counter when it has none")
    void cannotRemoveMissingOilCounter() {
        Permanent strider = addReadyStrider(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(strider), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates it and taps the crewing creature")
    void crewAnimatesIt() {
        Permanent strider = addReadyStrider(0);
        Permanent giant = addCreatureReady(player1, new FurnaceStrider());

        harness.activateAbility(player1, indexOf(strider), 1, null, null);
        harness.passBothPriorities();

        assertThat(giant.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, strider)).isTrue();
        assertThat(gqs.getEffectivePower(gd, strider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, strider)).isEqualTo(5);
    }

    @Test
    @DisplayName("Oil is paid immediately, but animation waits for resolution")
    void oilIsPaidBeforeResolution() {
        Permanent strider = addReadyStrider(1);

        harness.activateAbility(player1, indexOf(strider), 0, null, null);

        assertThat(strider.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.isCreature(gd, strider)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, strider)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Vehicle can use its oil ability")
    void tappedNewVehicleCanAnimate() {
        Permanent strider = addReadyStrider(1);
        strider.setSummoningSick(true);
        strider.tap();

        harness.activateAbility(player1, indexOf(strider), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, strider)).isTrue();
        assertThat(strider.isTapped()).isTrue();
        assertThat(strider.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("An animated Strider attacks without tapping")
    void animatedStriderHasVigilance() {
        Permanent strider = addReadyStrider(1);
        harness.activateAbility(player1, indexOf(strider), 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(indexOf(strider)));

        assertThat(strider.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew, and animation expires at end of turn")
    void summoningSickCreatureCanCrew() {
        Permanent strider = addReadyStrider(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FurnaceStrider());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, indexOf(strider), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, strider)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, strider)).isFalse();
    }

    @Test
    @DisplayName("A tapped creature cannot pay the crew cost")
    void tappedCreatureCannotCrew() {
        Permanent strider = addReadyStrider(0);
        Permanent creature = addCreatureReady(player1, new FurnaceStrider());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(strider), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, strider)).isFalse();
    }

    @Test
    @DisplayName("An animated Vehicle cannot crew itself")
    void animatedVehicleCannotCrewItself() {
        Permanent strider = addReadyStrider(1);
        harness.activateAbility(player1, indexOf(strider), 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(strider), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(strider.isTapped()).isFalse();
    }

    private Permanent addReadyStrider(int oilCounters) {
        Permanent strider = addCreatureReady(player1, new MeldwebStrider());
        strider.setCounterCount(CounterType.OIL, oilCounters);
        return strider;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
