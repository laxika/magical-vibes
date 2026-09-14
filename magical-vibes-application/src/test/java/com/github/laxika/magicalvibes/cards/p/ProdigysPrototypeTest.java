package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Weatherlight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProdigysPrototype.class, DuskLegionDreadnought.class, GrizzlyBears.class, Weatherlight.class})
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

        Permanent pilot = findPermanents(player1, "Pilot").getFirst();
        pilot.setSummoningSick(false);
        Permanent weatherlight = addVehicleReady(player1, new Weatherlight());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weatherlight), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
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
