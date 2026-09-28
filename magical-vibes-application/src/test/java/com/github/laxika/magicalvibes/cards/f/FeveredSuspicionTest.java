package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({FeveredSuspicion.class, Forest.class, Mountain.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(card));
        addFeveredSuspicionMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void castFeveredSuspicion() {
        harness.setHand(player1, List.of(new FeveredSuspicion()));
        addFeveredSuspicionMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void addFeveredSuspicionMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
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
