package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncientZiggurat;
import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.cards.e.ExoticOrchard;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.r.RealityTwist;
import com.github.laxika.magicalvibes.cards.u.UrzasMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FellwarStone.class, AncientZiggurat.class, ChromaticLantern.class, ExoticOrchard.class, Forest.class,
        Island.class, ManaReflection.class, RealityTwist.class, UrzasMine.class})
class FellwarStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no opponent land could produce colored mana")
    void producesNoManaWithoutOpponentLands() {
        harness.addToBattlefield(player1, new FellwarStone());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ignores an opponent's nonland permanents")
    void ignoresOpponentNonlandPermanents() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player2, new ChromaticLantern());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Auto-adds mana when only one opponent land color is available")
    void autoAddsManaWithSingleOpponentColor() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player2, new Forest()); // opponent's green source

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores an opponent land that can produce only colorless mana")
    void ignoresColorlessOnlyOpponentLand() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player2, new UrzasMine());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Includes mana abilities granted to an opponent's land")
    void includesAbilitiesGrantedToOpponentLand() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player2, new ChromaticLantern());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Mana Reflection doubles mana when one opponent land color is available")
    void manaReflectionDoublesAutomaticallyChosenColor() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mana Reflection doubles mana after choosing among opponent land colors")
    void manaReflectionDoublesChosenColor() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player1, new ManaReflection());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple opponent land colors are available")
    void promptsForChoiceWithMultipleColors() {
        harness.addToBattlefield(player1, new FellwarStone());
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
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player2, new Forest()); // green
        harness.addToBattlefield(player2, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Uses an opponent land's current replacement color")
    void usesCurrentReplacementColorOfOpponentLand() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player2, new RealityTwist());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A spend-restricted any-color opponent land still counts as a source of every color")
    void spendRestrictedOpponentLandContributesEveryColor() {
        harness.addToBattlefield(player1, new FellwarStone());
        // CR 106.7: what a land "could produce" is about the mana's type, and CR 106.6 says a spend
        // restriction doesn't affect the type — so Ziggurat's creature-spell-only mana still counts.
        harness.addToBattlefield(player2, new AncientZiggurat());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        // The mana Fellwar Stone itself adds is unrestricted.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller's own lands do not contribute colors")
    void ownLandsDoNotContribute() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player1, new Forest()); // controller's own land

        harness.activateAbility(player1, 0, null, null);

        // Only the controller's land could make colored mana, and it must be ignored
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
    @Test
    @DisplayName("An opponent's Exotic Orchard contributes colors from the controller's lands")
    void includesColorsOpponentOrchardCouldProduce() {
        harness.addToBattlefield(player1, new FellwarStone());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new ExoticOrchard());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped opponent land still contributes its mana color")
    void tappedOpponentLandStillContributesColor() {
        var stone = harness.addToBattlefieldAndReturn(player1, new FellwarStone());
        harness.addToBattlefieldAndReturn(player2, new Forest()).setTapped(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(stone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
