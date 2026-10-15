package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({SphereOfTheSuns.class})
class SphereOfTheSunsTest extends BaseCardTest {

    // ===== Entering the battlefield =====

    @Test
    @DisplayName("Enters the battlefield tapped with 3 charge counters")
    void entersWithThreeChargeCountersTapped() {
        harness.castFromHand(player1, new SphereOfTheSuns(), "{2}");
        harness.passBothPriorities();

        Permanent sphere = findPermanent(player1, "Sphere of the Suns");
        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(sphere.isTapped()).isTrue();
    }

    // ===== Activated ability: add mana of any color =====

    @Test
    @DisplayName("Activating ability removes a charge counter and prompts for mana color")
    void activateRemovesCounterAndPromptsForColor() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(sphere.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
            GameData gd = harness.getGameData();
            sphere.setCounterCount(CounterType.CHARGE, 3);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Can activate three times with 3 charge counters (untapping between uses)")
    void canActivateThreeTimes() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 3);

        // First activation
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        sphere.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        sphere.untap();

        // Third activation
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 3);

        // First activation taps it
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        // Cannot activate again while tapped
        assertThat(sphere.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Entering without being cast still gives three counters and enters tapped")
    void entersWithoutBeingCast() {
        Permanent sphere = harness.enterBattlefieldAndReturn(player1, new SphereOfTheSuns());

        assertThat(sphere.isTapped()).isTrue();
        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other counter types cannot pay the charge counter cost")
    void otherCountersCannotPayCost() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sphere.isTapped()).isFalse();
        assertThat(sphere.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Removing the last charge counter leaves the artifact on the battlefield")
    void lastCounterDoesNotSacrificeArtifact() {
        Permanent sphere = harness.addToBattlefieldAndReturn(player1, new SphereOfTheSuns());
        sphere.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.runStateBasedActions();

        assertThat(findPermanent(player1, "Sphere of the Suns")).isSameAs(sphere);
        assertThat(sphere.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        sphere.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sphere.isTapped()).isFalse();
    }

}
