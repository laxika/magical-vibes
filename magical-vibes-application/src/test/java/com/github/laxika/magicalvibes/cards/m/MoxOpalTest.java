package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoxOpal.class, Memnite.class, AccordersShield.class})
class MoxOpalTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate without three artifacts on the battlefield")
    void cannotActivateWithoutThreeArtifacts() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Memnite());

        // Only two controlled artifacts: metalcraft is not met.
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    @DisplayName("Can activate with three artifacts and prompts for color choice")
    void canActivateWithThreeArtifacts() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());

        // Three controlled artifacts, including Mox Opal, satisfy metalcraft.
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            gd = harness.getGameData();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new MoxOpal());
            harness.addToBattlefield(player1, new Memnite());
            harness.addToBattlefield(player1, new AccordersShield());

            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Opponents' artifacts do not satisfy metalcraft")
    void opponentsArtifactsDoNotCount() {
        var opal = harness.addToBattlefieldAndReturn(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new AccordersShield());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
        assertThat(opal.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapped artifacts count and mana resolves without using the stack")
    void tappedArtifactsCountAndManaResolvesImmediately() {
        var opal = harness.addToBattlefieldAndReturn(player1, new MoxOpal());
        harness.addToBattlefieldAndReturn(player1, new Memnite()).setTapped(true);
        harness.addToBattlefieldAndReturn(player1, new AccordersShield()).setTapped(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(opal.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("More than three artifacts also satisfy metalcraft")
    void moreThanThreeArtifactsAllowActivation() {
        harness.addToBattlefield(player1, new MoxOpal());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new AccordersShield());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
