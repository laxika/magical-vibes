package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.ProfessorOfSymbology;
import com.github.laxika.magicalvibes.cards.w.WaterfallAerialist;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MentorsGuidance.class, WaterfallAerialist.class, ProfessorOfSymbology.class})
class MentorsGuidanceTest extends BaseCardTest {

    @Test
    @DisplayName("Without a qualifying permanent, scries 1 and draws a card once")
    void withoutQualifyingPermanentResolvesNormally() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castMentorsGuidance();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A controlled Wizard copies Mentor's Guidance")
    void controlledWizardCopiesSpell() {
        harness.addToBattlefield(player1, new WaterfallAerialist());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castMentorsGuidance();
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's qualifying permanent does not cause a copy")
    void opponentPermanentDoesNotCopySpell() {
        harness.addToBattlefield(player2, new WaterfallAerialist());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castMentorsGuidance();
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Wizard entering after casting still enables the copy")
    void qualifyingPermanentAddedBeforeTriggerResolves() {
        castMentorsGuidance();
        harness.addToBattlefield(player1, new WaterfallAerialist());

        resolveCopiedSpell();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the only Wizard before the trigger resolves prevents copying")
    void qualifyingPermanentLostBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new WaterfallAerialist());
        castMentorsGuidance();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple qualifying permanents still produce only one copy")
    void multipleQualifyingPermanentsCopyOnlyOnce() {
        harness.addToBattlefield(player1, new WaterfallAerialist());
        harness.addToBattlefield(player1, new ProfessorOfSymbology());
        castMentorsGuidance();

        resolveCopiedSpell();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Cleric alone enables copying")
    void controlledClericCopiesSpell() {
        harness.addToBattlefield(player1, new ProfessorOfSymbology());
        castMentorsGuidance();

        resolveCopiedSpell();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Putting the scryed card on the bottom draws the next card")
    void scryBottomBeforeDrawing() {
        WaterfallAerialist top = new WaterfallAerialist();
        ProfessorOfSymbology next = new ProfessorOfSymbology();
        harness.setLibrary(player1, List.of(top, next));
        castMentorsGuidance();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the Wizard after copying does not remove the copy")
    void qualifyingPermanentLostAfterTriggerResolves() {
        harness.addToBattlefield(player1, new WaterfallAerialist());
        castMentorsGuidance();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void resolveCopiedSpell() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void castMentorsGuidance() {
        harness.castFromHand(player1, new MentorsGuidance(), "{2}{U}");
    }
}
