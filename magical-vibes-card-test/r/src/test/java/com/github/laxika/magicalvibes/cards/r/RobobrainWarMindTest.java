package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobobrainWarMind.class, CopperMyr.class, GrizzlyBears.class})
class RobobrainWarMindTest extends BaseCardTest {

    @Test
    void powerTracksControllerHandAndToughnessIsFive() {
        Permanent robobrain = addCreatureReady(player1, new RobobrainWarMind());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, robobrain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robobrain)).isEqualTo(5);
    }

    @Test
    void entersWithEnergyForEachArtifactCreatureControlled() {
        addCreatureReady(player1, new CopperMyr());
        harness.castFromHand(player1, new RobobrainWarMind(), "{3}{U}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void mayPayThreeEnergyToDrawOnAttack() {
        addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotPayWithoutThreeEnergy() {
        Permanent robobrain = addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, robobrain)).isZero();
    }

    @Test
    void decliningPaymentDoesNotDraw() {
        addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerHands.get(player1.getId()).clear();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void powerUpdatesWithHandChangesAndIgnoresOpponentsHand() {
        Permanent robobrain = addCreatureReady(player1, new RobobrainWarMind());
        harness.setHand(player1, List.of(new RobobrainWarMind()));
        harness.setHand(player2, List.of(new RobobrainWarMind(), new RobobrainWarMind()));

        assertThat(gqs.getEffectivePower(gd, robobrain)).isEqualTo(1);

        harness.setHand(player1, List.of(new RobobrainWarMind(), new RobobrainWarMind(),
                new RobobrainWarMind()));
        assertThat(gqs.getEffectivePower(gd, robobrain)).isEqualTo(3);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, robobrain)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, robobrain)).isEqualTo(5);
    }

    @Test
    void energyCountsOnlyControllersArtifactCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new RobobrainWarMind());
        harness.castFromHand(player1, new RobobrainWarMind(), "{3}{U}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void energyCountsArtifactCreaturesWhenTriggerResolvesEvenAfterSourceLeaves() {
        harness.castFromHand(player1, new RobobrainWarMind(), "{3}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).clear();
        addCreatureReady(player1, new RobobrainWarMind());
        addCreatureReady(player1, new RobobrainWarMind());
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void attackPaysExactlyThreeEnergyAndDrawIncreasesPower() {
        Permanent robobrain = addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 5);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RobobrainWarMind(), new RobobrainWarMind()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, robobrain)).isEqualTo(1);
    }

    @Test
    void canPayEnergyGainedAfterAttackTriggerIsCreated() {
        addCreatureReady(player1, new RobobrainWarMind());
        gd.playerEnergyCounters.put(player1.getId(), 0);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RobobrainWarMind()));

        declareAttackers(List.of(0));
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void powerIsDefinedInHandAndGraveyard() {
        RobobrainWarMind robobrain = new RobobrainWarMind();
        harness.setHand(player1, List.of(robobrain, new RobobrainWarMind()));
        harness.setHand(player2, List.of());

        assertThat(gqs.getEffectiveCardPower(gd, robobrain)).isEqualTo(2);

        harness.setHand(player1, List.of(new RobobrainWarMind()));
        gd.playerGraveyards.get(player1.getId()).add(robobrain);

        assertThat(gqs.getEffectiveCardPower(gd, robobrain)).isEqualTo(1);
    }
}
