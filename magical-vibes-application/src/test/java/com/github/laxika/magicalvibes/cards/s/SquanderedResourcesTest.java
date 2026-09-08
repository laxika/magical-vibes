package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GriffinCanyon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RootboundCrag;
import com.github.laxika.magicalvibes.cards.t.Taiga;
import com.github.laxika.magicalvibes.cards.t.ThawingGlaciers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquanderedResources.class, Forest.class, Island.class, RootboundCrag.class,
        ThawingGlaciers.class, Taiga.class, GriffinCanyon.class})
class SquanderedResourcesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest adds {G}")
    void sacrificeForestAddsGreen() {
        harness.addToBattlefield(player1, new SquanderedResources());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With several lands the chosen land decides the mana type")
    void chosenLandDecidesManaType() {
        harness.addToBattlefield(player1, new SquanderedResources());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, islandId);

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Sacrificing a dual land prompts for a type among those it could produce")
    void dualLandPromptsForType() {
        harness.addToBattlefield(player1, new SquanderedResources());
        Permanent crag = harness.addToBattlefieldAndReturn(player1, new RootboundCrag());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("GREEN", "RED");
        harness.assertInGraveyard(player1, crag.getCard().getName());

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Recognizes mana types from a single multi-color mana ability")
    void recognizesManaTypesFromSingleMultiColorManaAbility() {
        harness.addToBattlefield(player1, new SquanderedResources());
        harness.addToBattlefield(player1, new Taiga());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("GREEN", "RED");
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Taiga");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Sacrificing a colorless-producing land adds colorless mana")
    void sacrificeColorlessLandAddsColorless() {
        harness.addToBattlefield(player1, new SquanderedResources());
        harness.addToBattlefield(player1, new GriffinCanyon());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Griffin Canyon");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a land that produces no mana adds nothing")
    void landThatProducesNoManaAddsNothing() {
        harness.addToBattlefield(player1, new SquanderedResources());
        harness.addToBattlefield(player1, new ThawingGlaciers());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Thawing Glaciers");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new SquanderedResources());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
