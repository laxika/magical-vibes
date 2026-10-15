package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherHub.class})
class AetherHubTest extends BaseCardTest {

    @Test
    void entersWithOneEnergyCounter() {
        harness.setHand(player1, List.of(new AetherHub()));
        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent hub = addCreatureReady(player1, new AetherHub());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hub.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void paysEnergyAndTapsForAnyColorMana() {
        Permanent hub = addCreatureReady(player1, new AetherHub());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(hub.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTapForAnyColorWithoutEnergy() {
        addCreatureReady(player1, new AetherHub());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesEachColorAndPaysOnlyOneEnergy(ManaColor color) {
        Permanent hub = addCreatureReady(player1, new AetherHub());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(hub.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTapForColorlessBeforeEntryTriggerResolves() {
        harness.setHand(player1, List.of(new AetherHub()));
        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void colorlessAbilityDoesNotSpendEnergy() {
        addCreatureReady(player1, new AetherHub());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateTappedHubAndDoesNotSpendEnergy() {
        Permanent hub = addCreatureReady(player1, new AetherHub());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(hub.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
