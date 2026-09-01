package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlpharaelDreamingAcolyte.class, GrizzlyBears.class, JalumTome.class})
class AlpharaelDreamingAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and allows the discard to stop after an artifact")
    void drawsTwoCardsAndStopsAfterArtifactDiscard() {
        setDeck(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(
                new AlpharaelDreamingAcolyte(), new GrizzlyBears(), new JalumTome()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof JalumTome);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires two discards when no artifact is discarded")
    void requiresTwoDiscardsWithoutArtifact() {
        setDeck(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(
                new AlpharaelDreamingAcolyte(), new GrizzlyBears(), new GrizzlyBears()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Has deathtouch during its controller's turn only")
    void hasDeathtouchDuringItsControllersTurnOnly() {
        Permanent alpharael = harness.addToBattlefieldAndReturn(player1,
                new AlpharaelDreamingAcolyte());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, alpharael, Keyword.DEATHTOUCH)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, alpharael, Keyword.DEATHTOUCH)).isFalse();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void setDeck(List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
