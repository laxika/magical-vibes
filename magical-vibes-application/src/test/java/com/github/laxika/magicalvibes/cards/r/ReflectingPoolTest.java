package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectingPoolTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no other land you control could produce colored mana")
    void producesNoManaWithoutOtherLands() {
        harness.addToBattlefield(player1, new ReflectingPool());

        harness.activateAbility(player1, 0, null, null);

        // Reflecting Pool cannot tap for itself, so nothing is available
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one of your land colors is available")
    void autoAddsManaWithSingleColor() {
        harness.addToBattlefield(player1, new ReflectingPool());
        harness.addToBattlefield(player1, new Forest()); // green source

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple of your land colors are available")
    void promptsForChoiceWithMultipleColors() {
        harness.addToBattlefield(player1, new ReflectingPool());
        harness.addToBattlefield(player1, new Forest()); // green
        harness.addToBattlefield(player1, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color from your available land colors adds the correct mana")
    void choosingColorAddsMana() {
        harness.addToBattlefield(player1, new ReflectingPool());
        harness.addToBattlefield(player1, new Forest()); // green
        harness.addToBattlefield(player1, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent lands do not contribute colors")
    void opponentLandsDoNotContribute() {
        harness.addToBattlefield(player1, new ReflectingPool());
        harness.addToBattlefield(player2, new Forest()); // opponent's land

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
}
