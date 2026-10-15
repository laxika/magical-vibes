package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyosImmobilizer.class, PropheticPrism.class, CopperLonglegs.class, PhyrexianArena.class})
class TamiyosImmobilizerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four oil counters")
    void entersWithFourOilCounters() {
        harness.setHand(player1, List.of(new TamiyosImmobilizer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent immobilizer = findPermanent(player1, "Tamiyo's Immobilizer");
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing an oil counter taps target artifact")
    void removesOilCounterAndTapsArtifact() {
        Permanent immobilizer = addReadyImmobilizer(player1, 1);
        Permanent target = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isZero();
        assertThat(immobilizer.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap target creature")
    void canTapTargetCreature() {
        addReadyImmobilizer(player1, 1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addReadyImmobilizer(player1, 1);
        Permanent enchantment = addReadyEnchantment(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Cannot activate without an oil counter")
    void cannotActivateWithoutOilCounter() {
        addReadyImmobilizer(player1, 0);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    void costsArePaidBeforeTargetIsTapped() {
        Permanent immobilizer = addReadyImmobilizer(player1, 4);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(immobilizer.isTapped()).isTrue();
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void canTargetAnAlreadyTappedPermanent() {
        Permanent immobilizer = addReadyImmobilizer(player1, 2);
        Permanent target = addReadyCreature(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void canTargetItself() {
        Permanent immobilizer = addReadyImmobilizer(player1, 2);

        harness.activateAbility(player1, 0, null, immobilizer.getId());
        harness.passBothPriorities();

        assertThat(immobilizer.isTapped()).isTrue();
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void canActivateTheTurnItEnters() {
        Permanent immobilizer = harness.enterBattlefieldAndReturn(player1, new TamiyosImmobilizer());
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(immobilizer.isTapped()).isTrue();
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent immobilizer = addReadyImmobilizer(player1, 2);
        Permanent target = addReadyCreature(player2);
        immobilizer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void otherCounterTypesCannotPayTheOilCost() {
        Permanent immobilizer = addReadyImmobilizer(player1, 0);
        immobilizer.setCounterCount(CounterType.CHARGE, 4);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(immobilizer.isTapped()).isFalse();
        assertThat(immobilizer.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addReadyImmobilizer(Player player, int counters) {
        Permanent immobilizer = harness.addToBattlefieldAndReturn(player, new TamiyosImmobilizer());
        immobilizer.setSummoningSick(false);
        immobilizer.setCounterCount(CounterType.OIL, counters);
        return immobilizer;
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PropheticPrism());
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new CopperLonglegs());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new PhyrexianArena());
    }
}
