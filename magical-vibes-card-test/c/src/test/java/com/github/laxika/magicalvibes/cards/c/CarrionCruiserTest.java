package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarrionCruiser.class, DarksteelRelic.class, DuskLegionDreadnought.class,
        Forest.class, GrizzlyBears.class, Island.class})
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

    @Test
    @DisplayName("ETB can return a Vehicle milled by the same ability")
    void etbReturnsNewlyMilledVehicle() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new CarrionCruiser(), new Forest(), new Island()));
        harness.setLibrary(player2, List.of(new Forest(), new Island()));
        castAndResolve();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Carrion Cruiser");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB can return a creature milled from a one-card library")
    void etbReturnsNewlyMilledCreatureFromShortLibrary() {
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castAndResolve();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB still returns a card when the library is empty")
    void etbReturnsCardWithEmptyLibrary() {
        harness.setGraveyard(player1, List.of(new CarrionCruiser()));
        harness.setLibrary(player1, List.of());
        castAndResolve();
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Carrion Cruiser");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returning an eligible graveyard card cannot be declined")
    void etbReturnIsMandatory() {
        harness.setGraveyard(player1, List.of(new CarrionCruiser()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castAndResolve();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertInHand(player1, "Carrion Cruiser");
    }

    @Test
    @DisplayName("ETB rejects a noncreature artifact that is not a Vehicle")
    void etbRejectsIneligibleCard() {
        harness.setGraveyard(player1, List.of(new DarksteelRelic(), new CarrionCruiser()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castAndResolve();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInHand(player1, "Carrion Cruiser");
        harness.assertInGraveyard(player1, "Darksteel Relic");
    }

    @Test
    @DisplayName("A summoning-sick creature can crew a summoning-sick Vehicle")
    void summoningSicknessDoesNotPreventCrewing() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CarrionCruiser());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures and opposing creatures cannot pay crew")
    void crewRequiresUntappedControlledCreature() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent tappedCrew = addCreatureReady(player1, new GrizzlyBears());
        tappedCrew.tap();
        Permanent opposingCrew = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(opposingCrew.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew leaves the Vehicle untapped and animation expires at end of turn")
    void crewAnimationExpiresAtEndOfTurn() {
        Permanent vehicle = addVehicleReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(vehicle.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionCruiser(), "{2}{B}");
        harness.passBothPriorities();
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CarrionCruiser());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
