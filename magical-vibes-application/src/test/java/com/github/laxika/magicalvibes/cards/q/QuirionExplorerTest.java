package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.d.DromarsCavern;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuirionExplorer.class, DromarsCavern.class, TerminalMoraine.class, Forest.class})
class QuirionExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no opponent land could produce colored mana")
    void producesNoManaWithoutOpponentLands() {
        addCreatureReady(player1, new QuirionExplorer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one opponent land color is available")
    void autoAddsManaWithSingleOpponentColor() {
        Permanent explorer = addCreatureReady(player1, new QuirionExplorer());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(explorer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple opponent land colors are available")
    void promptsForChoiceWithMultipleColors() {
        addCreatureReady(player1, new QuirionExplorer());
        harness.addToBattlefield(player2, new DromarsCavern());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("BLACK", "BLUE", "WHITE");
    }

    @Test
    @DisplayName("Choosing an available opponent land color adds the correct mana")
    void choosingColorAddsMana() {
        addCreatureReady(player1, new QuirionExplorer());
        harness.addToBattlefield(player2, new DromarsCavern());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own lands do not contribute colors")
    void ownLandsDoNotContribute() {
        addCreatureReady(player1, new QuirionExplorer());
        harness.addToBattlefield(player1, new DromarsCavern());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ignores an opponent land that can produce only colorless mana")
    void ignoresOpponentLandThatProducesOnlyColorlessMana() {
        Permanent explorer = addCreatureReady(player1, new QuirionExplorer());
        harness.addToBattlefield(player2, new TerminalMoraine());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(explorer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while Quirion Explorer is summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new QuirionExplorer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Quirion Explorer is tapped")
    void cannotActivateWhileTapped() {
        Permanent explorer = addCreatureReady(player1, new QuirionExplorer());
        explorer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
