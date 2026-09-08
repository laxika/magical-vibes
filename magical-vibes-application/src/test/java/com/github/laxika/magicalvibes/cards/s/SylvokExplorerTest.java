package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SylvokExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no opponent land could produce colored mana")
    void producesNoManaWithoutOpponentLands() {
        addCreatureReady(player1, new SylvokExplorer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one opponent land color is available")
    void autoAddsManaWithSingleOpponentColor() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple opponent land colors are available")
    void promptsForChoiceWithMultipleColors() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing an available opponent land color adds the correct mana")
    void choosingColorAddsMana() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own lands do not contribute colors")
    void ownLandsDoNotContribute() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
}
