package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SearchForAzcanta;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscoveryDispersal.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Island.class, Ornithopter.class, SearchForAzcanta.class})
class DiscoveryDispersalTest extends BaseCardTest {

    @Test
    @DisplayName("Discovery surveils 2, then draws a card")
    void discoverySurveilsThenDraws() {
        Card topCard = new Island();
        Card secondCard = new Forest();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, drawnCard));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dispersal returns the opponent's greatest-mana-value nonland and makes them discard")
    void dispersalReturnsGreatestManaValueNonlandThenOpponentDiscards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Dispersal lets the opponent choose among tied greatest mana values")
    void dispersalOpponentChoosesTiedPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, second.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void discoveryCanUseBlackManaAndReordersKeptCardsBeforeDrawing() {
        Card first = new Island();
        Card second = new Forest();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dispersalCanBeCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player2, 0);
        }

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Discovery // Dispersal");
    }

    @Test
    void dispersalDiscardsEvenWhenOpponentControlsOnlyLands() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player2, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void dispersalCanDiscardThePermanentJustReturnedToAnEmptyHand() {
        Card creature = new HillGiant();
        harness.addToBattlefield(player2, creature);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        if (gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class) != null) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void dispersalDoesNotReturnLowerManaValueNonlandWhenTransformedLandHasGreatestManaValue() {
        SearchForAzcanta search = new SearchForAzcanta();
        Permanent azcanta = harness.addToBattlefieldAndReturn(player2, search);
        azcanta.setCard(search.getBackFaceCard());
        azcanta.setTransformed(true);
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player2, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(azcanta, thopter);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Every opponent chooses before the chosen permanents return together, then all choose and discard together (ruling)")
    void opponentsChooseBeforeAnythingReturnsOrIsDiscarded() {
        Player player3 = addThirdPlayer();
        harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player3, List.of(new Island()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handlePermanentChosen(player2, secondGiant.getId());

        PendingInteraction.PermanentChoice thirdPlayersChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(thirdPlayersChoice).isNotNull();
        assertThat(thirdPlayersChoice.playerId()).isEqualTo(player3.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondGiant);

        harness.handlePermanentChosen(player3, secondBears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(secondGiant);
        assertThat(gd.playerBattlefields.get(player3.getId())).doesNotContain(secondBears);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player3.getId());
        harness.assertInHand(player2, "Forest");
        harness.handleCardChosen(player3, 0);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player3, "Island");
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInHand(player3, "Grizzly Bears");
    }

    private Player addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        Player player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(
                new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
        return player3;
    }
}
