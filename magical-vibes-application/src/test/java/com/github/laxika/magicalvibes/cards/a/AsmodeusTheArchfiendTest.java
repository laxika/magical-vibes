package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
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

@CardUsed({AsmodeusTheArchfiend.class, HillGiantHerdgorger.class, MinimusContainment.class,
        YouComeToARiver.class})
class AsmodeusTheArchfiendTest extends BaseCardTest {

    @Test
    @DisplayName("Binding Contract replaces its controller's draw with a face-down exile")
    void replacesControllerDrawOnly() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        Card ownTop = new HillGiantHerdgorger();
        Card ownRemaining = new HillGiantHerdgorger();
        Card opponentTop = new HillGiantHerdgorger();
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
                new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger(),
                new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger());
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
        Card first = new HillGiantHerdgorger();
        Card second = new HillGiantHerdgorger();
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

    @Test
    @DisplayName("Binding Contract stops replacing draws when Asmodeus loses its abilities")
    void drawsNormallyUnderMinimusContainment() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        Card top = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new MinimusContainment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, asmodeus.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).isEmpty();
    }

    @Test
    @DisplayName("Neither player can see the identity of cards exiled by Binding Contract")
    void faceDownExiledCardsAreHiddenFromBothPlayers() {
        harness.addToBattlefield(player1, new AsmodeusTheArchfiend());
        Card top = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.clearMessages();

        harness.publishState();

        var controllerMessages = harness.getConn1().getMessagesContaining("GAME_STATE");
        var opponentMessages = harness.getConn2().getMessagesContaining("GAME_STATE");
        assertThat(controllerMessages).isNotEmpty();
        assertThat(opponentMessages).isNotEmpty();
        assertThat(controllerMessages.getLast()).doesNotContain(top.getId().toString());
        assertThat(opponentMessages.getLast()).doesNotContain(top.getId().toString());
    }

    @Test
    @DisplayName("Drawing seven with only two cards exiles two without losing to an empty library")
    void shortLibraryDoesNotCauseDrawLoss() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        List<Card> library = List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Returning no exiled cards loses no life")
    void emptyReturnLosesNoLife() {
        harness.addToBattlefield(player1, new AsmodeusTheArchfiend());
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The draw ability draws normally if Asmodeus leaves before it resolves")
    void drawAbilityResolvesAfterSourceLeaves() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        List<Card> library = List.of(
                new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger(),
                new HillGiantHerdgorger(), new HillGiantHerdgorger(), new HillGiantHerdgorger(),
                new HillGiantHerdgorger());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.castModalInstant(player1, 0, 0, List.of(asmodeus.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsAll(library);
        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).isEmpty();
    }

    @Test
    @DisplayName("The return ability still returns its cards if Asmodeus leaves before it resolves")
    void returnAbilityResolvesAfterSourceLeaves() {
        Permanent asmodeus = harness.addToBattlefieldAndReturn(player1, new AsmodeusTheArchfiend());
        Card top = new HillGiantHerdgorger();
        harness.setLibrary(player1, List.of(top));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.setHand(player1, List.of(new YouComeToARiver()));
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.castModalInstant(player1, 0, 0, List.of(asmodeus.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(top);
        assertThat(gd.getCardsExiledByPermanent(asmodeus.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }
}
