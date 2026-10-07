package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        TezzeretBetrayerOfFlesh.class,
        DuskLegionDreadnought.class,
        GrizzlyBears.class,
        Millstone.class,
        MindStone.class
})
class TezzeretBetrayerOfFleshTest extends BaseCardTest {

    @Test
    void firstArtifactAbilityEachTurnCostsTwoLess() {
        addReadyTezzeret(4);
        Permanent millstone = addCreatureReady(player1, new Millstone());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);

        millstone.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatingAnotherArtifactDoesNotGetAnotherDiscount() {
        addReadyTezzeret(4);
        addCreatureReady(player1, new Millstone());
        addCreatureReady(player1, new Millstone());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactManaAbilityConsumesTheDiscountForOtherArtifacts() {
        addReadyTezzeret(4);
        addCreatureReady(player1, new MindStone());
        addCreatureReady(player1, new Millstone());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void plusOneDiscardsTwoWhenThereAreNoArtifacts() {
        addReadyTezzeret(4);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void plusOneCanDeclineTheArtifactDiscard() {
        addReadyTezzeret(4);
        Card artifact = new MindStone();
        harness.setHand(player1, List.of(artifact, new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void minusTwoCanAnimateAnOpponentsArtifactPermanently() {
        Permanent tezzeret = addReadyTezzeret(4);
        Permanent artifact = addCreatureReady(player2, new MindStone());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(tezzeret);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }

    @Test
    void emblemDoesNotDrawForAnOpponentsArtifact() {
        addReadyTezzeret(6);
        addCreatureReady(player2, new MindStone());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void emblemDrawsAgainWhenTheSameArtifactBecomesTappedAgain() {
        addReadyTezzeret(6);
        Permanent artifact = addCreatureReady(player1, new MindStone());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        artifact.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void plusOneDrawsTwoAndAllowsDiscardingAnArtifactInstead() {
        addReadyTezzeret(4);
        Card artifact = new MindStone();
        harness.setHand(player1, List.of(artifact, new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void minusTwoSetsNonVehicleToFourFour() {
        addReadyTezzeret(4);
        Permanent artifact = addCreatureReady(player1, new MindStone());

        harness.activateAbility(player1, 0, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(4);
    }

    @Test
    void minusTwoKeepsVehiclePowerToughness() {
        addReadyTezzeret(4);
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        harness.activateAbility(player1, 0, 1, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);
    }

    @Test
    void ultimateDrawsWhenYourArtifactBecomesTapped() {
        addReadyTezzeret(6);
        addCreatureReady(player1, new MindStone());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyTezzeret(int loyalty) {
        Permanent permanent = addCreatureReady(player1, new TezzeretBetrayerOfFlesh());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
