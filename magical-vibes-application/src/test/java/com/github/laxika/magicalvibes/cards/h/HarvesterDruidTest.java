package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, HarvesterDruid.class, Island.class})
class HarvesterDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no land you control could produce colored mana")
    void producesNoManaWithoutLands() {
        addCreatureReady(player1, new HarvesterDruid());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one of your land colors is available")
    void autoAddsManaWithSingleColor() {
        addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana produced by the druid is tracked as creature mana")
    void tracksSingleColorManaAsCreatureMana() {
        addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped land still determines an available color")
    void tappedLandStillProvidesAvailableColor() {
        addCreatureReady(player1, new HarvesterDruid());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple of your land colors are available")
    void promptsForChoiceWithMultipleColors() {
        addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color from your available land colors adds the correct mana")
    void choosingColorAddsMana() {
        addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent lands do not contribute colors")
    void opponentLandsDoNotContribute() {
        addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("A tapped land still contributes a color it could produce")
    void tappedLandStillContributesItsColor() {
        Permanent druid = addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
