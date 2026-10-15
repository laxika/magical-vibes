package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingPhone.class, WrathOfGod.class, GrizzlyBears.class, HillGiant.class,
        Plains.class, Shock.class})
class LivingPhoneTest extends BaseCardTest {

    @Test
    @DisplayName("When Living Phone dies, it offers a creature with power 2 or less from the top five")
    void deathTriggerOffersSmallCreature() {
        harness.addToBattlefield(player1, new LivingPhone());
        Card smallCreature = new GrizzlyBears();
        Card powerTwoCreature = new GrizzlyBears();
        List<Card> topCards = List.of(smallCreature, new HillGiant(), new Shock(), powerTwoCreature,
                new Plains());
        destroyLivingPhone(topCards);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                smallCreature.getId(), powerTwoCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(smallCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(smallCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                topCards.get(1), topCards.get(2), topCards.get(3), topCards.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When no creature with power 2 or less is revealed, all five cards go to the bottom")
    void noSmallCreatureGoesToHand() {
        harness.addToBattlefield(player1, new LivingPhone());
        List<Card> topCards = List.of(new HillGiant(), new Shock(), new Plains(), new Shock(), new Plains());
        destroyLivingPhone(topCards);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The only eligible creature can be declined even with fewer than five cards")
    void mayDeclineOnlyEligibleCreatureInShortLibrary() {
        harness.addToBattlefield(player1, new LivingPhone());
        List<Card> topCards = List.of(new LivingPhone(), new Plains());
        destroyLivingPhone(topCards);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top five are considered and the rest go below the untouched library")
    void remainingCardsGoBelowUntouchedLibrary() {
        harness.addToBattlefield(player1, new LivingPhone());
        Card chosen = new LivingPhone();
        Card sixth = new LivingPhone();
        Card seventh = new Plains();
        List<Card> library = List.of(chosen, new HillGiant(), new Shock(), new Plains(), new HillGiant(),
                sixth, seventh);
        List<Card> opponentLibrary = List.of(new LivingPhone(), new Plains());
        harness.setLibrary(player2, opponentLibrary);
        destroyLivingPhone(library);

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(chosen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        List<Card> remainingLibrary = gd.playerDecks.get(player1.getId());
        assertThat(remainingLibrary.subList(0, 2)).containsExactly(sixth, seventh);
        assertThat(remainingLibrary.subList(2, remainingLibrary.size()))
                .containsExactlyInAnyOrderElementsOf(library.subList(1, 5));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not cause a draw or leave a pending choice")
    void emptyLibraryFinishesTrigger() {
        harness.addToBattlefield(player1, new LivingPhone());
        destroyLivingPhone(List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The final card of a library can be revealed and put into hand")
    void mayChooseCreatureFromSingleCardLibrary() {
        harness.addToBattlefield(player1, new LivingPhone());
        Card chosen = new LivingPhone();
        destroyLivingPhone(List.of(chosen));

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Living Phone uses that opponent's library and hand")
    void opponentDeathTriggerUsesOpponentLibrary() {
        harness.addToBattlefield(player2, new LivingPhone());
        Card chosen = new LivingPhone();
        harness.setLibrary(player2, List.of(chosen));
        List<Card> ownLibrary = List.of(new LivingPhone(), new Plains());
        destroyLivingPhone(ownLibrary);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ownLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void destroyLivingPhone(List<Card> topCards) {
        harness.setLibrary(player1, topCards);
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
