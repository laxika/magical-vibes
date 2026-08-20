package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WanderingMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player1, new WanderingMage());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castMentorsGuidance();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
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
        harness.addToBattlefield(player2, new WanderingMage());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castMentorsGuidance();
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castMentorsGuidance() {
        harness.setHand(player1, List.of(new MentorsGuidance()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
    }
}
