package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LizardfolkLibrarians.class, GrizzlyBears.class})
class LizardfolkLibrariansTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by scrying two")
    void entersWithScryTwo() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new LizardfolkLibrarians()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
