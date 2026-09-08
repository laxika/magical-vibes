package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyImmobilizer(Player player, int counters) {
        Permanent immobilizer = new Permanent(new TamiyosImmobilizer());
        immobilizer.setSummoningSick(false);
        immobilizer.setCounterCount(CounterType.OIL, counters);
        gd.playerBattlefields.get(player.getId()).add(immobilizer);
        return immobilizer;
    }

    private Permanent addReadyArtifact(Player player) {
        return addReady(player, new Permanent(new AngelsFeather()));
    }

    private Permanent addReadyCreature(Player player) {
        return addReady(player, new Permanent(new GrizzlyBears()));
    }

    private Permanent addReadyEnchantment(Player player) {
        return addReady(player, new Permanent(new Pacifism()));
    }

    private Permanent addReady(Player player, Permanent permanent) {
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
