package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Soulblast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeveredSuspicion.class, Forest.class, Mountain.class, GrizzlyBears.class,
        Shock.class, Soulblast.class})
class FeveredSuspicionTest extends BaseCardTest {

    @Test
    void exilesEachOpponentsLibraryThroughANonlandAndOffersTheSpells() {
        Card ownForest = new Forest();
        Card opponentMountain = new Mountain();
        Card opponentBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownForest));
        harness.setLibrary(player2, List.of(opponentMountain, opponentBears));
        castFeveredSuspicion();

        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentBears.getId());
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .contains(opponentMountain, opponentBears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownForest);
    }

    @Test
    void castsAnyNumberOfNonlandCardsWithoutPayingManaCosts() {
        Player player3 = addThirdPlayer();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first));
        harness.setLibrary(player3, List.of(second));
        castFeveredSuspicion();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.stack).extracting(entry -> entry.getCard())
                .contains(first, second);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card())
                .doesNotContain(first, second);
    }

    @Test
    void reboundExilesTheSpellAndOffersItAtTheNextUpkeep() {
        FeveredSuspicion card = new FeveredSuspicion();
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, card, "{6}{B}{R}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void leavesUncastCardsExiledAndStopsAtTheFirstNonland() {
        Card land = new Mountain();
        Card offered = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setLibrary(player2, List.of(land, offered, remaining));
        castFeveredSuspicion();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.findExiledCard(land.getId()).ownerId()).isEqualTo(player2.getId());
        assertThat(gd.findExiledCard(offered.getId()).ownerId()).isEqualTo(player2.getId());
    }

    @Test
    void exhaustsAnAllLandLibraryWithoutOfferingACast() {
        Card land = new Mountain();
        harness.setLibrary(player2, List.of(land));
        castFeveredSuspicion();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void stolenInstantGoesToItsOwnersGraveyard() {
        Card shock = new Shock();
        harness.setLibrary(player2, List.of(shock));
        castFeveredSuspicion();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    void canPayAnAdditionalSacrificeCostForAnExiledSpell() {
        Card soulblast = new Soulblast();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(soulblast));
        castFeveredSuspicion();

        harness.handleMultipleCardsChosen(player1, List.of(soulblast.getId()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void reboundCastsAgainAndThenGoesToTheGraveyard() {
        FeveredSuspicion card = new FeveredSuspicion();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, card, "{6}{B}{R}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesTheSpellExiled() {
        FeveredSuspicion card = new FeveredSuspicion();
        harness.setLibrary(player2, List.of());
        harness.castFromHand(player1, card, "{6}{B}{R}");
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void castFeveredSuspicion() {
        harness.castFromHand(player1, new FeveredSuspicion(), "{6}{B}{R}");
        harness.passBothPriorities();
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
