package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeylineOfAnticipation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DredgeTheMire.class, GrizzlyBears.class, Island.class,
        GrafdiggersCage.class, DesecratedTomb.class, LeylineOfAnticipation.class})
class DredgeTheMireTest extends BaseCardTest {

    @Test
    void opponentChoosesCreatureFromTheirGraveyardAndItEntersUnderYourControl() {
        Card opponentCreature = new GrizzlyBears();
        Card opponentLand = new Island();
        Card controllerCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentLand, opponentCreature));
        harness.setGraveyard(player1, List.of(controllerCreature));
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.cardPool()).containsExactly(opponentCreature);

        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void ignoresCreatureCardsInControllerGraveyardAndNoncreaturesInOpponentGraveyard() {
        Card controllerCreature = new GrizzlyBears();
        Card opponentLand = new Island();
        harness.setGraveyard(player1, List.of(controllerCreature));
        harness.setGraveyard(player2, List.of(opponentLand));
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controllerCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void opponentChoosesExactlyOneOfMultipleCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card chosenCreature = new GrizzlyBears();
        Card land = new Island();
        harness.setGraveyard(player2, List.of(land, firstCreature, chosenCreature));

        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).containsExactly(firstCreature, chosenCreature);
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(chosenCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land, firstCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentCannotDeclineWhenACreatureIsAvailable() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.handleGraveyardCardChosen(player2, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(creature);
    }

    @Test
    void emptyOpponentGraveyardDoesNotRequireAChoice() {
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof DredgeTheMire);
    }

    @Test
    void creaturePreventedFromEnteringDoesNotTriggerLeavingTheGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.addToBattlefield(player2, new DesecratedTomb());
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).noneMatch(card -> card == creature);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Bat");
    }

    @Test
    void allOpponentsChooseBeforeTheirCreaturesEnterTogether() {
        Player player3 = addOpponent();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstCreature));
        harness.setGraveyard(player3, List.of(secondCreature));
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        harness.handleGraveyardCardChosen(player2, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player3.getId());
        harness.handleGraveyardCardChosen(player3, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactlyInAnyOrder(firstCreature, secondCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player3.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player3.getId())).isEmpty();
    }

    @Test
    void offTurnChoicesStartWithTheActiveOpponent() {
        Player player3 = addOpponent();
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setGraveyard(player3, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        harness.forceActivePlayer(player3);
        harness.castFromHand(player1, new DredgeTheMire(), "{3}{B}");
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player3.getId());
        harness.handleGraveyardCardChosen(player3, 0);
        choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
    }

    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        gd.playerIds.add(opponent.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerNames.add(opponent.getUsername());
        gd.playerIdToName.put(opponent.getId(), opponent.getUsername());
        gd.playerDecks.put(opponent.getId(), new ArrayList<>());
        gd.playerHands.put(opponent.getId(), new ArrayList<>());
        gd.playerGraveyards.put(opponent.getId(), new ArrayList<>());
        gd.playerBattlefields.put(opponent.getId(), new ArrayList<>());
        gd.playerCommandZones.put(opponent.getId(), new ArrayList<>());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(opponent.getId(), 20);
        return opponent;
    }
}
