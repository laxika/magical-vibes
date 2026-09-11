package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AsmodeusTheArchfiend.class, GrizzlyBears.class})
class AsmodeusTheArchfiendTest extends BaseCardTest {

    @Test
    @DisplayName("Binding Contract replaces its controller's draw with a face-down exile")
    void replacesControllerDrawOnly() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        Card ownTop = new GrizzlyBears();
        Card ownRemaining = new GrizzlyBears();
        Card opponentTop = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownTop, ownRemaining));
        harness.setLibrary(player2, List.of(opponentTop));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownRemaining);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownTop);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentTop);

        ExiledCardEntry entry = gd.findExiledCard(ownTop.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.ownerId()).isEqualTo(player1.getId());
        assertThat(entry.sourcePermanentId()).isEqualTo(asmodeus.getId());
        assertThat(entry.faceDown()).isTrue();
    }

    @Test
    @DisplayName("Drawing seven cards exiles all seven cards instead")
    void drawSevenIsReplacedOneCardAtATime() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).containsExactlyElementsOf(library);
        assertThat(gd.getExiledWithPermanentEntries(asmodeus.getId(), asmodeus.getCard().getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Returning the exiled cards puts them into their owners' hands and loses that much life")
    void returnsCardsAndLosesLifeForReturnedCards() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("An empty library is not a draw loss under Binding Contract")
    void emptyLibraryExilesNothingWithoutLosing() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.winnerPlayerId).isNull();
    }
}
