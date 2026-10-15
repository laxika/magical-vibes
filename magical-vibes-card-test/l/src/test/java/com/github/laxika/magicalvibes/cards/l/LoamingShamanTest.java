package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AquastrandSpider;
import com.github.laxika.magicalvibes.cards.a.AuroraEidolon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoamingShaman.class, AquastrandSpider.class, AuroraEidolon.class})
class LoamingShamanTest extends BaseCardTest {

    private void castShaman() {
        harness.castFromHand(player1, new LoamingShaman(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB targets a player before choosing cards from that graveyard")
    void targetsPlayerThenCards() {
        Card spider = new AquastrandSpider();
        Card eidolon = new AuroraEidolon();
        harness.setGraveyard(player2, List.of(spider, eidolon));

        castShaman();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(spider.getId(), eidolon.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Shuffles any number of cards from the targeted graveyard")
    void shufflesSelectedCardsFromTargetedGraveyard() {
        Card spider = new AquastrandSpider();
        Card eidolon = new AuroraEidolon();
        Card remaining = new AquastrandSpider();
        harness.setGraveyard(player2, List.of(spider, eidolon, remaining));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spider.getId(), eidolon.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 2);
    }

    @Test
    @DisplayName("Can target its controller's graveyard")
    void canTargetItsControllersGraveyard() {
        Card spider = new AquastrandSpider();
        Card eidolon = new AuroraEidolon();
        harness.setGraveyard(player1, List.of(spider, eidolon));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        castShaman();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spider.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(eidolon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 1);
    }

    @Test
    @DisplayName("Choosing zero cards still shuffles the targeted player's library")
    void choosingZeroCardsLeavesGraveyardCardsInPlace() {
        Card spider = new AquastrandSpider();
        harness.setGraveyard(player2, List.of(spider));

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spider);
        harness.assertOnBattlefield(player1, "Loaming Shaman");
    }

    @Test
    @DisplayName("Targeting a player with no graveyard cards needs no card-choice prompt")
    void emptyTargetGraveyardNeedsNoCardChoice() {
        castShaman();

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Loaming Shaman");
    }

    @Test
    @DisplayName("Can shuffle every card in the targeted graveyard")
    void canSelectEveryGraveyardCard() {
        Card spider = new AquastrandSpider();
        Card eidolon = new AuroraEidolon();
        harness.setGraveyard(player2, List.of(spider, eidolon));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spider.getId(), eidolon.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySizeBefore + 2).contains(spider, eidolon);
    }

    @Test
    @DisplayName("Still shuffles remaining legal cards when another selected card leaves")
    void selectedCardLeavingDoesNotPreventOtherCardsMoving() {
        Card spider = new AquastrandSpider();
        Card eidolon = new AuroraEidolon();
        harness.setGraveyard(player2, List.of(spider, eidolon));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spider.getId(), eidolon.getId()));
        harness.setGraveyard(player2, List.of(eidolon));
        harness.setExile(player2, List.of(spider));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySizeBefore + 1).contains(eidolon).doesNotContain(spider);
        assertThat(gd.findExiledCard(spider.getId())).isNotNull();
    }

    @Test
    @DisplayName("Does not shuffle cards that enter the graveyard after targets are chosen")
    void laterGraveyardCardsAreNotSelectedAutomatically() {
        Card spider = new AquastrandSpider();
        Card eidolon = new AuroraEidolon();
        harness.setGraveyard(player2, List.of(spider));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spider.getId()));
        harness.setGraveyard(player2, List.of(spider, eidolon));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(eidolon);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySizeBefore + 1).contains(spider).doesNotContain(eidolon);
    }
}
