package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FearlessSwashbucklerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Vehicles haste")
    void givesOwnVehiclesHaste() {
        harness.addToBattlefieldAndReturn(player1, new FearlessSwashbuckler());
        Permanent ownVehicle = addDreadnoughtReady();
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentVehicle, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Draws three cards and discards two when a Pirate and Vehicle attack")
    void drawsAndDiscardsWhenPirateAndVehicleAttack() {
        addCreatureReady(player1, new FearlessSwashbuckler());
        addDreadnoughtReady();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card()));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only a Pirate attacks")
    void doesNotTriggerWithoutVehicleAttacking() {
        addCreatureReady(player1, new FearlessSwashbuckler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    private Permanent addDreadnoughtReady() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        vehicle.setAnimatedUntilEndOfTurn(true);
        return vehicle;
    }
}
