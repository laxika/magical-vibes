package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentumBreaker.class, AirResponseUnit.class, BrightfieldGlider.class, Island.class})
class MomentumBreakerTest extends BaseCardTest {

    @Test
    void entersAndOpponentChoosesCreatureOrVehicleToSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrightfieldGlider());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(vehicle.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature).doesNotContain(vehicle);
        harness.assertInGraveyard(player2, "Air Response Unit");
    }

    @Test
    void opponentWithoutCreatureOrVehicleDiscardsInstead() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player2, List.of(new BrightfieldGlider(), new Island()));
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player2, "Brightfield Glider");
    }

    @Test
    void sacrificeAbilityGainsCurrentSpeed() {
        Permanent breaker = harness.addToBattlefieldAndReturn(player1, new MomentumBreaker());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(breaker);
    }

    @Test
    void opponentMayChooseCreatureAndKeepVehicleAndHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BrightfieldGlider());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        harness.setHand(player2, List.of(new Island()));
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vehicle).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Brightfield Glider");
        harness.assertInHand(player2, "Island");
    }

    @Test
    void soleCreatureIsSacrificedWithoutDiscarding() {
        harness.addToBattlefield(player2, new BrightfieldGlider());
        harness.setHand(player2, List.of(new Island()));
        harness.addToBattlefield(player1, new BrightfieldGlider());
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Brightfield Glider");
        harness.assertInHand(player2, "Island");
        harness.assertOnBattlefield(player1, "Brightfield Glider");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void soleUncrewedVehicleIsSacrificedWithoutDiscarding() {
        harness.addToBattlefield(player2, new AirResponseUnit());
        harness.setHand(player2, List.of(new Island()));
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Air Response Unit");
        harness.assertInHand(player2, "Island");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentWithNothingToSacrificeAndEmptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Island());
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player1, "Momentum Breaker");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringStartsEnginesAndSacrificingDoesNotRemoveSpeed() {
        harness.setHand(player2, List.of());
        castMomentumBreaker();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Momentum Breaker");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void lifeGainUsesSpeedAtResolutionRatherThanActivation() {
        harness.addToBattlefield(player1, new MomentumBreaker());
        gd.playerSpeeds.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    private void castMomentumBreaker() {
        harness.castFromHand(player1, new MomentumBreaker(), "{1}{B}");
    }
}
