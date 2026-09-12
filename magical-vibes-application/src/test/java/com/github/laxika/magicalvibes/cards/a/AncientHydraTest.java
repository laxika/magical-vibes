package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FlintGolem;
import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientHydra.class, FlintGolem.class, KorHaven.class})
class AncientHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Ancient Hydra enters with five fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new AncientHydra(), "{4}{R}");
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
    @DisplayName("Fading removes the last fade counter without sacrificing Ancient Hydra")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent hydra = addCreatureReady(player1, new AncientHydra());
        hydra.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Ancient Hydra");
    }

    @Test
    @DisplayName("Fading does not remove a fade counter during an opponent's upkeep")
    void doesNotRemoveFadeCounterDuringOpponentsUpkeep() {
        Permanent hydra = addCreatureReady(player1, new AncientHydra());
        hydra.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player2);
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
        Permanent target = addCreatureReady(player2, new FlintGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.FADE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage ability can target a player")
    void dealsDamageToPlayer() {
        Permanent hydra = addCreatureReady(player1, new AncientHydra());
        hydra.setCounterCount(CounterType.FADE, 1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.FADE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The damage ability cannot be activated without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        addCreatureReady(player1, new AncientHydra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The damage ability cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new AncientHydra());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KorHaven());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
