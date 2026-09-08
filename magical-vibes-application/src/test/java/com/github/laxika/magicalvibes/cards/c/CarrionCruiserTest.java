package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarrionCruiserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills two cards and returns a creature or Vehicle from the graveyard")
    void etbMillsAndReturnsCreatureOrVehicle() {
        DuskLegionDreadnought vehicle = new DuskLegionDreadnought();
        harness.setGraveyard(player1, List.of(vehicle, new DarksteelRelic()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castAndResolve();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Dusk Legion Dreadnought");
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("ETB can return a creature card and excludes other noncreature cards")
    void etbReturnsCreatureAndFiltersCards() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new DarksteelRelic(), creature));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castAndResolve();

        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("Crew 1 animates Carrion Cruiser and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not prompt when the graveyard has no creature or Vehicle")
    void etbDoesNothingWithoutMatchingCard() {
        harness.setGraveyard(player1, List.of(new DarksteelRelic()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castAndResolve();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CarrionCruiser()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new CarrionCruiser());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
