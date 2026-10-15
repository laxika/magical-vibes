package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustHarvester.class, FountainOfYouth.class, GrizzlyBears.class})
class RustHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an artifact, grows, and deals damage equal to its new power")
    void exilesArtifactGrowsAndDealsPowerDamageToPlayer() {
        Permanent harvester = addReadyHarvester();
        FountainOfYouth artifactCard = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifactCard));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifactCard);
        assertThat(harvester.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Deals its new power as damage to a creature")
    void dealsNewPowerDamageToCreature() {
        Permanent harvester = addReadyHarvester();
        FountainOfYouth artifactCard = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifactCard));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without an artifact card in the graveyard")
    void cannotActivateWithoutArtifactCard() {
        addReadyHarvester();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void paysCostsBeforeResolvingAndCanExileAnArtifactCreature() {
        Permanent harvester = addReadyHarvester();
        RustHarvester artifact = new RustHarvester();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(harvester.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 18);
    }

    @Test
    void canTargetItselfAndDiesFromItsOwnDamage() {
        Permanent harvester = addReadyHarvester();
        harness.setGraveyard(player1, List.of(new RustHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, harvester.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rust Harvester");
        harness.assertInGraveyard(player1, "Rust Harvester");
    }

    @Test
    void usesLastKnownPowerWhenSourceLeavesBeforeResolution() {
        Permanent harvester = addReadyHarvester();
        harvester.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setGraveyard(player1, List.of(new RustHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, harvester));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Rust Harvester");
    }

    @Test
    void doesNotGrowWhenItsOnlyTargetLeavesBeforeResolution() {
        Permanent harvester = addReadyHarvester();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RustHarvester());
        RustHarvester artifact = new RustHarvester();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
        assertThat(harvester.isTapped()).isTrue();
    }

    @Test
    void cannotPayCostWithAnOpponentsArtifact() {
        addReadyHarvester();
        harness.setGraveyard(player2, List.of(new RustHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void usesPowerAtResolutionRatherThanAtActivation() {
        Permanent harvester = addReadyHarvester();
        harness.setGraveyard(player1, List.of(new RustHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harvester.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(harvester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertLife(player2, 16);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RustHarvester());
        harness.setGraveyard(player1, List.of(new RustHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHarvester() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new RustHarvester());
        harvester.setSummoningSick(false);
        return harvester;
    }
}
