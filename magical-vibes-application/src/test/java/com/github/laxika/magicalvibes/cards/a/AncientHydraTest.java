package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncientHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Ancient Hydra enters with five fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new AncientHydra()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Ancient Hydra");
        assertThat(hydra.getCounterCount(CounterType.FADE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent hydra = addCreatureReady(player1, new AncientHydra());
        hydra.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ancient Hydra");
    }

    @Test
    @DisplayName("Fading sacrifices Ancient Hydra when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new AncientHydra());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ancient Hydra");
    }

    @Test
    @DisplayName("Removing a fade counter and paying one mana deals 1 damage to any target")
    void removesCounterAndDealsDamage() {
        Permanent hydra = addCreatureReady(player1, new AncientHydra());
        hydra.setCounterCount(CounterType.FADE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.FADE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage ability cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new AncientHydra());
        Permanent forest = addCreatureReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
