package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.s.SteelHellkite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerDepot.class, CopperMyr.class, GrizzlyBears.class, StoneRain.class, SteelHellkite.class})
class PowerDepotTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a +1/+1 counter")
    void entersTappedWithCounter() {
        harness.setHand(player1, List.of(new PowerDepot()));

        harness.playLand(player1, 0);

        Permanent depot = findPermanent(player1, "Power Depot");
        assertThat(depot.isTapped()).isTrue();
        assertThat(depot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana abilities produce colorless or artifact-restricted mana")
    void producesMana() {
        Permanent depot = addReadyDepot();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        depot.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifact-restricted mana can cast artifact spells but not nonartifact spells")
    void artifactRestrictedMana() {
        Permanent depot = addReadyDepot();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.BLUE)).isZero();
        harness.passBothPriorities();

        depot.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Modular may move its counter to an artifact creature when it dies")
    void modularMovesCounterOnDeath() {
        Permanent depot = addReadyDepot();
        depot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent myr = addCreatureReady(player1, new CopperMyr());

        destroyDepot(depot);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(myr.getId());

        harness.handlePermanentChosen(player1, myr.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void restrictedManaPaysForArtifactAbility() {
        addReadyDepot();
        Permanent hellkite = addCreatureReady(player1, new SteelHellkite());
        int originalPower = gqs.getEffectivePower(gd, hellkite);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hellkite)).isEqualTo(originalPower + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    void colorlessManaCanCastNonartifactSpell() {
        addReadyDepot();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    void modularStillTriggersWithoutCounters() {
        Permanent depot = addReadyDepot();
        Permanent myr = addCreatureReady(player1, new CopperMyr());

        destroyDepot(depot);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(myr.getId());
        harness.handlePermanentChosen(player1, myr.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularCanGiveAllCountersToOpponentsArtifactCreature() {
        Permanent depot = addReadyDepot();
        depot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent myr = addCreatureReady(player2, new CopperMyr());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherDepot = harness.addToBattlefieldAndReturn(player1, new PowerDepot());

        destroyDepot(depot);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(myr.getId())
                .doesNotContain(bears.getId(), otherDepot.getId());
        harness.handlePermanentChosen(player1, myr.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularCounterPlacementCanBeDeclined() {
        Permanent depot = addReadyDepot();
        depot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent myr = addCreatureReady(player1, new CopperMyr());

        destroyDepot(depot);
        harness.handlePermanentChosen(player1, myr.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularWithNoLegalTargetDoesNotPrompt() {
        Permanent depot = addReadyDepot();
        depot.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new PowerDepot());

        destroyDepot(depot);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDepot() {
        Permanent depot = harness.addToBattlefieldAndReturn(player1, new PowerDepot());
        depot.setSummoningSick(false);
        depot.untap();
        return depot;
    }

    private void destroyDepot(Permanent depot) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, depot.getId());
        harness.passBothPriorities();
    }
}
