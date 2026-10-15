package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.h.Hylderblade;
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

@CardUsed({AlpharaelDreamingAcolyte.class, Island.class, Hylderblade.class})
class AlpharaelDreamingAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards and allows the discard to stop after an artifact")
    void drawsTwoCardsAndStopsAfterArtifactDiscard() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(
                new AlpharaelDreamingAcolyte(), new Island(), new Hylderblade()));
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
                .anyMatch(card -> card instanceof Hylderblade);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires two discards when no artifact is discarded")
    void requiresTwoDiscardsWithoutArtifact() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(
                new AlpharaelDreamingAcolyte(), new Island(), new Island()));
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

    @Test
    @DisplayName("May discard two cards even when the first is an artifact")
    void canContinueDiscardingAfterArtifact() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new AlpharaelDreamingAcolyte(), new Hylderblade()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2)
                .anyMatch(card -> card instanceof Hylderblade)
                .anyMatch(card -> card instanceof Island);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can discard an artifact drawn by the enters ability")
    void canDiscardNewlyDrawnArtifact() {
        harness.setLibrary(player1, List.of(new Hylderblade(), new Island()));
        harness.setHand(player1, List.of(new AlpharaelDreamingAcolyte()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Island.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(Hylderblade.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The enters ability still resolves after Alpharael leaves")
    void entersAbilityResolvesAfterSourceLeaves() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new AlpharaelDreamingAcolyte()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent alpharael = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.getPermanentRemovalService().removePermanentToHand(gd, alpharael);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .isInstanceOf(AlpharaelDreamingAcolyte.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deathtouch follows the opposing controller's turn and expires again")
    void deathtouchUsesOpposingControllersTurn() {
        Permanent alpharael = harness.addToBattlefieldAndReturn(player2,
                new AlpharaelDreamingAcolyte());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, alpharael, Keyword.DEATHTOUCH)).isFalse();
        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, alpharael, Keyword.DEATHTOUCH)).isTrue();
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, alpharael, Keyword.DEATHTOUCH)).isFalse();
    }

}
