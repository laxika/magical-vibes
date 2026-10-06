package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarkhansDragonfire.class, Shock.class, LlanowarElves.class, Island.class, Plains.class, GrizzlyBears.class})
class SarkhansDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage, then offers one red card from the top five")
    void dealsDamageAndOffersRedCard() {
        Card redCard = new Shock();
        Card greenCard = new LlanowarElves();
        Card blueCard = new Island();
        Card whiteCard = new Plains();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(redCard, greenCard, blueCard, whiteCard, creature));

        harness.setHand(player1, List.of(new SarkhansDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(redCard.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(redCard.getId()));

        harness.assertInHand(player1, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(greenCard, blueCard, whiteCard, creature);
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }

    @Test
    void mayDeclineRedCardAndBottomOnlyTheTopFive() {
        Card redCard = new Shock();
        List<Card> topFive = List.of(redCard, new Island(), new Plains(), new Island(), new Plains());
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), untouched));
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }

    @Test
    void noRedCardsAreTakenAndRemainingLibraryStaysOnTop() {
        List<Card> topFive = List.of(new Island(), new Plains(), new Island(), new Plains(), new Island());
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(topFive.get(0), topFive.get(1), topFive.get(2),
                topFive.get(3), topFive.get(4), untouched));
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }

    @Test
    void choosesOnlyOneRedCardFromAShortLibrary() {
        Card chosen = new Shock();
        Card otherRed = new Shock();
        Card land = new Island();
        harness.setLibrary(player1, List.of(chosen, otherRed, land));
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(otherRed, land);
        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }

    @Test
    void dealsDamageWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }

    @Test
    void illegalDamageTargetPreventsLookingAtTheLibrary() {
        Card redCard = new Shock();
        Card land = new Island();
        harness.setLibrary(player1, List.of(redCard, land));
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareSpell();

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(redCard, land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Sarkhan's Dragonfire");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SarkhansDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
