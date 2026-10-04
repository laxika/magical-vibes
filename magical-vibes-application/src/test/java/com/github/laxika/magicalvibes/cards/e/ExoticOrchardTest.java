package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AncientZiggurat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExoticOrchard.class, Forest.class, Island.class, AncientZiggurat.class,
        ReliquaryTower.class, RuptureSpire.class})
class ExoticOrchardTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no opponent land could produce colored mana")
    void producesNoManaWithoutOpponentLands() {
        harness.addToBattlefield(player1, new ExoticOrchard());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one opponent land color is available")
    void autoAddsManaWithSingleOpponentColor() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new Forest()); // opponent's green source

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple opponent land colors are available")
    void promptsForChoiceWithMultipleColors() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new Forest()); // green
        harness.addToBattlefield(player2, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color from the available opponent land colors adds the correct mana")
    void choosingColorAddsMana() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new Forest()); // green
        harness.addToBattlefield(player2, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own lands do not contribute colors")
    void ownLandsDoNotContribute() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player1, new Forest()); // controller's own land

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("An opposing Orchard can derive green from the controller's Forest")
    void opposingOrchardCanDeriveColorFromOwnForest() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new ExoticOrchard());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Orchards without another mana source produce no mana")
    void orchardsAloneProduceNoMana() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new ExoticOrchard());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped opposing lands still contribute their mana colors")
    void tappedOpponentLandContributesColor() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefieldAndReturn(player2, new Forest()).tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colorless opposing lands do not supply a color")
    void colorlessOpponentLandProducesNoMana() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new ReliquaryTower());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opposing any-color land offers exactly the five colors")
    void anyColorOpponentLandOffersAllColors() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new RuptureSpire());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opposing land's spending restriction does not restrict Orchard mana")
    void opponentSpendingRestrictionDoesNotCarryOver() {
        harness.addToBattlefield(player1, new ExoticOrchard());
        harness.addToBattlefield(player2, new AncientZiggurat());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "BLACK");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
