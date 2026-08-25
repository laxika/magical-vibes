package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AwakenTheMaelstrom;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfAlara.class, AwakenTheMaelstrom.class, CounselOfTheSoratami.class,
        GrizzlyBears.class, HillGiant.class, Plains.class})
class InvasionOfAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles until it finds two qualifying cards and offers one for a free cast")
    void findsTwoQualifyingCardsAndCastsOne() {
        CounselOfTheSoratami castCard = new CounselOfTheSoratami();
        GrizzlyBears handCard = new GrizzlyBears();
        HillGiant secondQualifyingCard = new HillGiant();
        Plains land = new Plains();
        setLibrary(List.of(land, secondQualifyingCard, castCard, handCard));
        castInvasion();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(secondQualifyingCard, castCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerHands.get(player1.getId())).contains(secondQualifyingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, handCard);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == castCard
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Offers the only qualifying card when the library has fewer than two")
    void offersOnlyOneQualifyingCard() {
        CounselOfTheSoratami onlyQualifyingCard = new CounselOfTheSoratami();
        Plains land = new Plains();
        setLibrary(List.of(land, onlyQualifyingCard));
        castInvasion();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(onlyQualifyingCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == onlyQualifyingCard
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("The free-cast choice may be declined, then one qualifying card goes to hand")
    void declineFreeCastPutsOneQualifyingCardIntoHand() {
        CounselOfTheSoratami first = new CounselOfTheSoratami();
        GrizzlyBears second = new GrizzlyBears();
        setLibrary(List.of(first, second));
        castInvasion();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castInvasion() {
        harness.setHand(player1, List.of(new InvasionOfAlara()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        gs.playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
