package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValorsFlagship.class, DuskLegionDreadnought.class, GrizzlyBears.class, Memnite.class,
        Stifle.class, CanopySpider.class})
class ValorsFlagshipTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling for X creates X enhanced Pilot tokens and draws a card")
    void cyclingCreatesPilotsAndDraws() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null, 2);
        resolveAllTriggers();

        List<Permanent> pilots = findPermanents(player1, "Pilot");
        assertThat(pilots).hasSize(2);
        assertThat(pilots).allSatisfy(pilot -> {
            assertThat(pilot.getCard().getPower()).isEqualTo(1);
            assertThat(pilot.getCard().getToughness()).isEqualTo(1);
        });
        harness.assertInGraveyard(player1, "Valor's Flagship");
        harness.assertInHand(player1, "Grizzly Bears");

        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        pilots.get(0).setSummoningSick(false);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.handlePermanentChosen(player1, pilots.get(0).getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilots.get(0).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew 3 animates Valor's Flagship")
    void crewAnimatesFlagship() {
        Permanent flagship = harness.addToBattlefieldAndReturn(player1, new ValorsFlagship());
        flagship.setSummoningSick(false);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent memnite = addCreatureReady(player1, new Memnite());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(flagship), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(memnite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling creates Pilots before drawing, with a priority window between them")
    void pilotsResolveSeparatelyFromCyclingDraw() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new ValorsFlagship()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null, 2);

        harness.assertInGraveyard(player1, "Valor's Flagship");
        assertThat(countPermanents(player1, "Pilot")).isZero();
        harness.assertNotInHand(player1, "Valor's Flagship");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Pilot")).isEqualTo(2);
        harness.assertNotInHand(player1, "Valor's Flagship");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Valor's Flagship");
    }

    @Test
    @DisplayName("Cycling with X zero draws a card without creating Pilots")
    void cyclingForZeroCreatesNoPilots() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new ValorsFlagship()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pilot")).isZero();
        harness.assertInGraveyard(player1, "Valor's Flagship");
        harness.assertInHand(player1, "Valor's Flagship");
    }

    @Test
    @DisplayName("A newly created Pilot can pay crew 3 without gaining actual power")
    void singleNewPilotCrewsFlagship() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new ValorsFlagship()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, null, 1);
        resolveAllTriggers();

        Permanent pilot = findPermanent(player1, "Pilot");
        Permanent flagship = harness.addToBattlefieldAndReturn(player1, new ValorsFlagship());
        assertThat(gqs.isCreature(gd, flagship)).isFalse();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(flagship), null, null);

        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, flagship)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling must pay both X and the fixed mana cost before discarding")
    void insufficientCyclingManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 2))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Valor's Flagship");
        harness.assertNotInGraveyard(player1, "Valor's Flagship");
        assertThat(countPermanents(player1, "Pilot")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Countering the cycling draw does not counter the Pilot trigger")
    void counteringCyclingStillCreatesPilots() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new ValorsFlagship()));
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null, 2);

        UUID cyclingId = gd.stack.getFirst().getTargetableId();
        harness.castInstant(player2, 0, cyclingId);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pilot")).isEqualTo(2);
        harness.assertNotInHand(player1, "Valor's Flagship");
        harness.assertInGraveyard(player1, "Valor's Flagship");
    }

    @Test
    @DisplayName("Countering the Pilot trigger does not counter the cycling draw")
    void counteringPilotTriggerStillDraws() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new ValorsFlagship()));
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null, 2);

        UUID pilotTriggerId = gd.stack.getLast().getTargetableId();
        harness.castInstant(player2, 0, pilotTriggerId);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pilot")).isZero();
        harness.assertInHand(player1, "Valor's Flagship");
        harness.assertInGraveyard(player1, "Valor's Flagship");
    }

    @Test
    @DisplayName("A crewed Flagship evades ground blockers and gains life from combat damage")
    void crewedFlagshipHasFlyingAndLifelink() {
        addCreatureReady(player1, new ValorsFlagship());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            gs.declareBlockers(gd, player2, List.of());
            resolveCombat();
        });

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 13);

        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Valor's Flagship"))).isFalse();
    }

    @Test
    @DisplayName("First strike kills a reach blocker before it can damage the Flagship")
    void firstStrikePreventsBlockerDamage() {
        Permanent flagship = addCreatureReady(player1, new ValorsFlagship());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player2, new CanopySpider());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveCombat();
        });

        harness.assertInGraveyard(player2, "Canopy Spider");
        harness.assertOnBattlefield(player1, "Valor's Flagship");
        assertThat(flagship.getMarkedDamage()).isZero();
        harness.assertLife(player1, 27);
        harness.assertLife(player2, 20);
    }
}
