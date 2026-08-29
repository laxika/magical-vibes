package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MomentumBreakerTest extends BaseCardTest {

    @Test
    void entersAndOpponentChoosesCreatureOrVehicleToSacrifice() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new AirResponseUnit());
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(vehicle.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear).doesNotContain(vehicle);
    }

    @Test
    void opponentWithoutCreatureOrVehicleDiscardsInstead() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Island()));
        castMomentumBreaker();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player2, "Grizzly Bears");
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

    private void castMomentumBreaker() {
        harness.setHand(player1, List.of(new MomentumBreaker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
    }
}
