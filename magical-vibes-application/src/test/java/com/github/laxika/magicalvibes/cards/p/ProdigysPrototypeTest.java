package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Weatherlight;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ProdigysPrototype.class, DuskLegionDreadnought.class, GrizzlyBears.class,
        Weatherlight.class, TrainedArynx.class, JukaiPreserver.class})
class ProdigysPrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one enhanced Pilot when one or more Vehicles attack")
    void createsOnePilotWhenVehiclesAttack() {
        Permanent prototype = addVehicleReady(player1, new ProdigysPrototype());
        Permanent dreadnought = addVehicleReady(player1, new DuskLegionDreadnought());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        crew(prototype, firstBear);
        crew(dreadnought, secondBear);
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pilot")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when only a non-Vehicle creature attacks")
    void doesNotTriggerForNonVehicleAttacker() {
        addVehicleReady(player1, new ProdigysPrototype());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created Pilot crews a Vehicle as though its power were two greater")
    void createdPilotEnhancesCrewPower() {
        Permanent prototype = addVehicleReady(player1, new ProdigysPrototype());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        crew(prototype, bear);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent pilot = findPermanent(player1, "Pilot");
        pilot.setSummoningSick(false);
        Permanent weatherlight = addVehicleReady(player1, new Weatherlight());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weatherlight), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An uncrewed Prototype triggers when another Vehicle attacks")
    void uncrewedPrototypeSeesAnotherVehicleAttack() {
        Permanent stationary = addVehicleReady(player1, new ProdigysPrototype());
        Permanent attacker = addVehicleReady(player1, new ProdigysPrototype());
        Permanent bear = addCreatureReady(player1, new JukaiPreserver());
        crew(attacker, bear);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, stationary)).isFalse();
        assertThat(findPermanents(player1, "Pilot")).hasSize(2);
    }

    @Test
    @DisplayName("An opposing Vehicle attacking does not trigger Prototype")
    void doesNotTriggerForOpposingVehicle() {
        addVehicleReady(player1, new ProdigysPrototype());
        Permanent attacker = addVehicleReady(player2, new ProdigysPrototype());
        addCreatureReady(player2, new JukaiPreserver());
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, attacker)).isTrue();

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pilot")).isEmpty();
        assertThat(findPermanents(player2, "Pilot")).hasSize(1);
    }

    @Test
    @DisplayName("A newly created Pilot can crew without haste and remains a 1/1")
    void freshPilotCanCrew() {
        Permanent attacker = addVehicleReady(player1, new ProdigysPrototype());
        Permanent bear = addCreatureReady(player1, new JukaiPreserver());
        crew(attacker, bear);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent pilot = findPermanent(player1, "Pilot");
        Permanent vehicle = addVehicleReady(player1, new ProdigysPrototype());

        crew(vehicle, pilot);

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Pilot's crew bonus does not help pay a saddle cost")
    void pilotCannotPaySaddleTwoAlone() {
        Permanent attacker = addVehicleReady(player1, new ProdigysPrototype());
        Permanent bear = addCreatureReady(player1, new JukaiPreserver());
        crew(attacker, bear);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent pilot = findPermanent(player1, "Pilot");
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerBattlefields.get(player1.getId()).remove(bear);
        Permanent mount = addCreatureReady(player1, new TrainedArynx());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mount.isSaddled()).isFalse();
        assertThat(pilot.isTapped()).isFalse();
    }

    private Permanent addVehicleReady(Player player, Card card) {
        return addCreatureReady(player, card);
    }

    private void crew(Permanent vehicle, Permanent crewer) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        harness.passBothPriorities();
    }
}
