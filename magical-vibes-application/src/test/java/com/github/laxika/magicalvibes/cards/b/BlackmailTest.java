package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.ProwlingPangolin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Blackmail.class, ElvishWarrior.class, Forest.class, GlorySeeker.class, ProwlingPangolin.class})
class BlackmailTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Target player may be the caster")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new Blackmail(), new GlorySeeker(), new ElvishWarrior())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blackmail");
        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertInHand(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Target player chooses which three cards to reveal")
    void targetChoosesThreeToReveal() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GlorySeeker(), new ElvishWarrior(), new ProwlingPangolin(), new Forest())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isTrue();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(3);
        assertThat(choice.validIndices()).containsExactly(0, 1, 2, 3);
    }

    @Test
    @DisplayName("Controller discards one of the three revealed cards; others stay in hand")
    void controllerDiscardsOneRevealed() {
        Card glorySeeker = new GlorySeeker();
        Card elvishWarrior = new ElvishWarrior();
        Card pangolin = new ProwlingPangolin();
        Card forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(glorySeeker, elvishWarrior, pangolin, forest)));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);

        PendingInteraction.RevealCardsDiscardChoice discardChoice = activeChoice();
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.revealStage()).isFalse();
        assertThat(discardChoice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discardChoice.validIndices()).containsExactly(0, 1, 2);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Blackmail");
        harness.assertInGraveyard(player2, "Elvish Warrior");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Glory Seeker", "Prowling Pangolin", "Forest");
    }

    @Test
    @DisplayName("Controller cannot choose during the reveal stage")
    void controllerCannotChooseDuringRevealStage() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GlorySeeker(), new ElvishWarrior(), new ProwlingPangolin(), new Forest())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("With three or fewer cards the whole hand is revealed, no reveal choice")
    void wholeHandRevealedWhenThreeOrFewer() {
        Card glorySeeker = new GlorySeeker();
        Card elvishWarrior = new ElvishWarrior();
        harness.setHand(player2, new ArrayList<>(List.of(glorySeeker, elvishWarrior)));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.RevealCardsDiscardChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.revealStage()).isFalse();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Glory Seeker");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Elvish Warrior");
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("empty")).isTrue();
    }

    @Test
    @DisplayName("Invalid revealed-card index is rejected in the discard stage")
    void invalidDiscardIndexRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GlorySeeker(), new ElvishWarrior())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Revealed cards and the discard are logged")
    void revealAndDiscardLogged() {
        harness.setHand(player2, new ArrayList<>(List.of(new GlorySeeker())));
        harness.setHand(player1, List.of(new Blackmail()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Glory Seeker"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("discards") && log.contains("Glory Seeker"));
    }
}
