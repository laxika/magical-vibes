package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AugurOfBolas.class, Divination.class, GrizzlyBears.class, Plains.class, Shock.class})
class AugurOfBolasTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only the instant and sorcery cards among the top three")
    void etbOffersOnlyInstantsAndSorceries() {
        setupTopCards(List.of(new Shock(), new Divination(), new Plains()));
        castAndResolveEtb();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactlyInAnyOrder("Shock", "Divination");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing the instant puts it into hand, rest go on bottom")
    void choosingInstantPutsIntoHand() {
        setupTopCards(List.of(new Shock(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Shock");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Declining puts nothing in hand and orders all three on bottom")
    void decliningReordersAllToBottom() {
        setupTopCards(List.of(new Shock(), new GrizzlyBears(), new Plains()));
        castAndResolveEtb();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertNotInHand(player1, "Shock");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("With no instant or sorcery among the top three, they are put on bottom directly")
    void noSpellReordersDirectly() {
        setupTopCards(List.of(new GrizzlyBears(), new Plains(), new GrizzlyBears()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    void choosingSorceryPreservesUnlookedCardsAndOrdersRestOnBottom() {
        Card creature = new AugurOfBolas();
        Card sorcery = new Divination();
        Card land = new Plains();
        Card unlooked = new Divination();
        setupTopCards(List.of(creature, sorcery, land, unlooked));
        castAndResolveEtb();

        harness.handleCardChosen(player1, 0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unlooked, land, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningLeavesFourthCardOnTopAndOrdersAllLookedCardsOnBottom() {
        Card sorcery = new Divination();
        Card creature = new AugurOfBolas();
        Card land = new Plains();
        Card unlooked = new Divination();
        setupTopCards(List.of(sorcery, creature, land, unlooked));
        castAndResolveEtb();

        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unlooked, land, sorcery, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void spellBelowTopThreeIsNotOffered() {
        Card first = new Plains();
        Card second = new AugurOfBolas();
        Card third = new Plains();
        Card unlooked = new Divination();
        setupTopCards(List.of(first, second, third, unlooked));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unlooked, third, second, first);
    }

    @Test
    void twoCardLibraryAllowsSelectionAndReturnsSingleRemainingCard() {
        Card land = new Plains();
        Card sorcery = new Divination();
        setupTopCards(List.of(land, sorcery));
        castAndResolveEtb();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyEligibleCardCanStillBeDeclined() {
        Card sorcery = new Divination();
        setupTopCards(List.of(sorcery));
        castAndResolveEtb();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryFinishesWithoutChoiceOrDraw() {
        setupTopCards(List.of());
        castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Augur of Bolas");
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new AugurOfBolas()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → library look
    }
}
