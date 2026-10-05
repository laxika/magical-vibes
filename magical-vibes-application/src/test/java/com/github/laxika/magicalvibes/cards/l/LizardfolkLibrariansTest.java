package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LizardfolkLibrarians.class})
class LizardfolkLibrariansTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by scrying two")
    void entersWithScryTwo() {
        Card topCard = new LizardfolkLibrarians();
        Card secondCard = new LizardfolkLibrarians();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.castFromHand(player1, new LizardfolkLibrarians(), "{3}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, topCard);
    }

    @Test
    @DisplayName("Double team conjures a copy and removes itself from both cards")
    void doubleTeamConjuresCopyAndRemovesItselfFromBothCards() {
        Permanent librarians = addCreatureReady(player1, new LizardfolkLibrarians());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, librarians, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Lizardfolk Librarians"))
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }

    @Test
    @DisplayName("Scry can put both cards on the bottom in either order")
    void scryPutsBothCardsBelowRemainingLibrary() {
        Card first = new LizardfolkLibrarians();
        Card second = new LizardfolkLibrarians();
        Card third = new LizardfolkLibrarians();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new LizardfolkLibrarians(), "{3}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    @DisplayName("Scry with an empty library completes without a choice")
    void scryWithEmptyLibraryCompletes() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new LizardfolkLibrarians(), "{3}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Lizardfolk Librarians");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second attack by the original does not conjure another card")
    void originalCannotDoubleTeamAgain() {
        Permanent librarians = addCreatureReady(player1, new LizardfolkLibrarians());
        harness.setHand(player1, List.of());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.performUntapStep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, librarians, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    @DisplayName("The conjured duplicate still scries on entry but cannot double team")
    void conjuredDuplicateScriesButDoesNotConjureAgain() {
        addCreatureReady(player1, new LizardfolkLibrarians());
        harness.setHand(player1, List.of());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        Card top = new LizardfolkLibrarians();
        harness.setLibrary(player1, List.of(top));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, duplicate, "{3}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);

        harness.performUntapStep(player1);
        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
