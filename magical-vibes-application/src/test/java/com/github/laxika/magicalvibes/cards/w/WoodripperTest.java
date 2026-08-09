package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WoodripperTest extends BaseCardTest {

    @Test
    @DisplayName("Woodripper enters with three fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new Woodripper()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent woodripper = findPermanent(player1, "Woodripper");
        assertThat(woodripper.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent woodripper = addReadyWoodripper(player1, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Woodripper");
    }

    @Test
    @DisplayName("Fading sacrifices Woodripper when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addReadyWoodripper(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Woodripper");
    }

    @Test
    @DisplayName("The activated ability removes a fade counter and destroys target artifact")
    void destroysTargetArtifact() {
        Permanent woodripper = addReadyWoodripper(player1, 1);
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(woodripper.getCounterCount(CounterType.FADE)).isZero();
        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("The activated ability cannot target a creature")
    void cannotTargetCreature() {
        addReadyWoodripper(player1, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activated ability cannot be paid without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        Permanent woodripper = addReadyWoodripper(player1, 0);
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThat(woodripper.getCounterCount(CounterType.FADE)).isZero();
    }

    private Permanent addReadyWoodripper(com.github.laxika.magicalvibes.model.Player player, int fadeCounters) {
        Permanent woodripper = addCreatureReady(player, new Woodripper());
        woodripper.setCounterCount(CounterType.FADE, fadeCounters);
        return woodripper;
    }
}
