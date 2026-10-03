package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.d.DaringMechanic;
import com.github.laxika.magicalvibes.cards.f.FloodTheEngine;
import com.github.laxika.magicalvibes.cards.g.GreenbeltGuardian;
import com.github.laxika.magicalvibes.cards.r.RoverBlades;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaradoraHeartOfAlacria.class, BrightfieldGlider.class, DaringMechanic.class,
        RoverBlades.class, GreenbeltGuardian.class, FloodTheEngine.class})
class CaradoraHeartOfAlacriaTest extends BaseCardTest {

    @Test
    @DisplayName("searches for a Mount or Vehicle")
    void searchesForMountOrVehicle() {
        castCaradora();
        harness.setLibrary(player1, List.of(new BrightfieldGlider(), new RoverBlades(), new GreenbeltGuardian()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.getSubtypes().contains(CardSubtype.MOUNT)
                || card.getSubtypes().contains(CardSubtype.VEHICLE));

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("adds a counter to a Vehicle you control")
    void addsCounterToControlledVehicle() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new RoverBlades());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("does not add a counter to an opponent's Vehicle")
    void doesNotAddCounterToOpponentsVehicle() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new RoverBlades());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canDeclineSearch() {
        castCaradora();
        BrightfieldGlider mount = new BrightfieldGlider();
        harness.setLibrary(player1, List.of(mount));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mount);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSearchForVehicleAndRevealIt() {
        castCaradora();
        RoverBlades vehicle = new RoverBlades();
        harness.setLibrary(player1, List.of(vehicle, new DaringMechanic()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(vehicle);
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vehicle);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(vehicle).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindEvenWhenMountIsAvailable() {
        castCaradora();
        BrightfieldGlider mount = new BrightfieldGlider();
        harness.setLibrary(player1, List.of(mount));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mount);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void addsOneCounterToBatchOnNonMountCreature() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void addsCounterToMountEvenWhenOpponentPlacesIt() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        harness.addToBattlefield(player2, new DaringMechanic());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, mount.getId());
        harness.passBothPriorities();

        assertThat(mount.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotAddCountersToOpponentsNonMountCreature() {
        harness.addToBattlefield(player1, new CaradoraHeartOfAlacria());
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new GreenbeltGuardian());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void stopsAddingCountersWhenCaradoraLosesAllAbilities() {
        Permanent caradora = harness.addToBattlefieldAndReturn(player1, new CaradoraHeartOfAlacria());
        harness.addToBattlefield(player1, new DaringMechanic());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new RoverBlades());
        harness.setHand(player1, List.of(new FloodTheEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, caradora.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castCaradora() {
        harness.setHand(player1, List.of(new CaradoraHeartOfAlacria()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
