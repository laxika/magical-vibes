package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.UrzasMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvokExplorer.class, Forest.class, Island.class, UrzasMine.class})
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

    @Test
    @DisplayName("Ignores an opponent land that can produce only colorless mana")
    void ignoresOpponentLandThatProducesOnlyColorlessMana() {
        Permanent explorer = addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player2, new UrzasMine());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(explorer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while Sylvok Explorer is summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SylvokExplorer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Sylvok Explorer is tapped")
    void cannotActivateWhileTapped() {
        Permanent explorer = addCreatureReady(player1, new SylvokExplorer());
        explorer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped opponent lands still contribute colors and mana is added immediately")
    void tappedOpponentLandStillContributes() {
        Permanent explorer = addCreatureReady(player1, new SylvokExplorer());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(explorer.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's nonland mana source does not contribute colors")
    void opponentNonlandManaSourceDoesNotContribute() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player1, new Forest());
        addCreatureReady(player2, new SylvokExplorer());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The color choice excludes colorless mana from opponent lands")
    void choiceExcludesColorlessMana() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player2, new UrzasMine());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("GREEN", "BLUE");

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose a color that no opponent land could produce")
    void rejectsUnavailableColor() {
        addCreatureReady(player1, new SylvokExplorer());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "RED"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
