package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadlyBrewTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesBeforeAllSelectedPermanentsAreSacrificedAndCanReturnAnotherPermanent() {
        Permanent player1Sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent player2Sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card player1Return = new Forest();
        Card player2Return = new Forest();
        harness.setGraveyard(player1, List.of(player1Return));
        harness.setGraveyard(player2, List.of(player2Return));
        castDeadlyBrew();

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice player1Choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.context()).isInstanceOf(
                MultiPermanentChoiceContext.EachPlayerSacrificesCreatureOrPlaneswalkerThenMayReturnChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Sacrifice.getId()));
        PendingInteraction.MultiPermanentChoice player2Choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Sacrifice.getId()));

        PendingInteraction.GraveyardChoice player1ReturnChoice =
                gameData.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(player1ReturnChoice.playerId()).isEqualTo(player1.getId());
        harness.handleGraveyardCardChosen(player1, player1ReturnChoice.validIndices().getFirst());

        PendingInteraction.GraveyardChoice player2ReturnChoice =
                gameData.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(player2ReturnChoice.playerId()).isEqualTo(player2.getId());
        harness.handleGraveyardCardChosen(player2, player2ReturnChoice.validIndices().getFirst());

        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gameData.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(player1Sacrifice.getId()));
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(player2Sacrifice.getId()));
        assertThat(gameData.playerHands.get(player1.getId())).contains(player1Return);
        assertThat(gameData.playerHands.get(player2.getId())).contains(player2Return);
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(player1Return.getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(player2Return.getId()));
    }

    @Test
    void sacrificedCardCannotBeReturnedAndReturnChoiceCanBeDeclined() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        castDeadlyBrew();

        GameData gameData = harness.getGameData();
        PendingInteraction.GraveyardChoice player1ReturnChoice =
                gameData.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(player1ReturnChoice).isNotNull();
        assertThat(player1ReturnChoice.validIndices()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, -1);

        PendingInteraction.GraveyardChoice player2ReturnChoice =
                gameData.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(player2ReturnChoice).isNotNull();
        harness.handleGraveyardCardChosen(player2, -1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gameData.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameData.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNothingWhenPlayersControlNoCreatureOrPlaneswalker() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castDeadlyBrew();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    private void castDeadlyBrew() {
        harness.setHand(player1, List.of(new DeadlyBrew()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
