package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrbanDaggertooth.class, GrizzlyBears.class, Shock.class})
class UrbanDaggertoothTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt damage, proliferates")
    void proliferatesWhenDealtDamage() {
        harness.addToBattlefield(player2, new UrbanDaggertooth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        UUID daggertoothId = harness.getPermanentId(player2, "Urban Daggertooth");
        harness.castAndResolveInstant(player1, 0, daggertoothId);
        harness.passBothPriorities(); // Resolve the enrage trigger
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Urban Daggertooth")).isNotNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate adds each existing counter kind to selected permanents and players")
    void proliferatesPermanentsAndPlayersOfEitherController() {
        Permanent daggertooth = harness.addToBattlefieldAndReturn(player2, new UrbanDaggertooth());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new UrbanDaggertooth());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        daggertooth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, daggertooth.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId(), player1.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(daggertooth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The controller may choose no permanents or players to proliferate")
    void canChooseNothing() {
        Permanent daggertooth = harness.addToBattlefieldAndReturn(player2, new UrbanDaggertooth());
        daggertooth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, daggertooth.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(daggertooth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Separate damage events each proliferate, including damage that kills Urban Daggertooth")
    void triggersForEachDamageEventIncludingLethalDamage() {
        Permanent daggertooth = harness.addToBattlefieldAndReturn(player2, new UrbanDaggertooth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, daggertooth.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, daggertooth.getId());
        harness.assertInGraveyard(player2, "Urban Daggertooth");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage triggers proliferate")
    void proliferatesFromCombatDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent daggertooth = addCreatureReady(player2, new UrbanDaggertooth());
        daggertooth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(daggertooth.getId()));

        assertThat(daggertooth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Urban Daggertooth");
    }

    @Test
    @DisplayName("Vigilance keeps Urban Daggertooth untapped when it attacks")
    void attacksWithoutTapping() {
        Permanent daggertooth = addCreatureReady(player1, new UrbanDaggertooth());

        declareAttackers(List.of(0));

        assertThat(daggertooth.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Proliferate finishes normally when no permanent or player has counters")
    void resolvesWithoutEligibleCounters() {
        Permanent daggertooth = harness.addToBattlefieldAndReturn(player2, new UrbanDaggertooth());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, daggertooth.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Urban Daggertooth");
    }
}
