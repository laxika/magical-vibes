package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SHIELDFlyingCar.class, GrizzlyBears.class, Forest.class})
class SHIELDFlyingCarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one creature you control and returns it at the next end step")
    void exilesAndReturnsOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID originalId = bears.getId();
        castFlyingCar(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(originalId);
    }

    @Test
    @DisplayName("ETB can resolve without choosing a creature")
    void canResolveWithoutTarget() {
        harness.setHand(player1, List.of(new SHIELDFlyingCar()));
        addFlyingCarMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "S.H.I.E.L.D. Flying Car");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target a creature an opponent controls")
    void rejectsOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SHIELDFlyingCar()));
        addFlyingCarMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void rejectsNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SHIELDFlyingCar()));
        addFlyingCarMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Crew 1 animates the Vehicle and taps the creature used to crew it")
    void crewAnimatesVehicle() {
        Permanent vehicle = addReadyFlyingCar();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private void castFlyingCar(UUID targetId) {
        harness.setHand(player1, List.of(new SHIELDFlyingCar()));
        addFlyingCarMana();
        harness.castArtifact(player1, 0, targetId);
    }

    private void addFlyingCarMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyFlyingCar() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SHIELDFlyingCar());
        vehicle.setSummoningSick(false);
        return vehicle;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
