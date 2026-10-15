package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PatchworkAutomaton;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NetworkTerminal.class, PatchworkAutomaton.class, Forest.class})
class NetworkTerminalTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Network Terminal adds one mana of a chosen color")
    void tapsForChosenColor() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(terminal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping another artifact lets Network Terminal draw, then discard")
    void tapsAnotherArtifactAndLoots() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Card keptCard = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of(keptCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(terminal.isTapped()).isTrue();
        assertThat(other.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(keptCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot tap Network Terminal itself as the other artifact")
    void requiresAnotherUntappedArtifact() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot use a tapped artifact to pay the ability's artifact cost")
    void requiresUntappedArtifact() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        other.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opposing artifact cannot pay the additional tap cost")
    void requiresArtifactYouControl() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
        assertThat(opposingArtifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped land cannot pay the artifact tap cost")
    void requiresArtifactRatherThanLand() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A newly controlled artifact creature can pay the additional tap cost")
    void canTapSummoningSickArtifactCreature() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new PatchworkAutomaton());
        automaton.setSummoningSick(true);
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(terminal.isTapped()).isTrue();
        assertThat(automaton.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The card just drawn can be chosen for discard")
    void canDiscardDrawnCard() {
        harness.addToBattlefield(player1, new NetworkTerminal());
        harness.addToBattlefield(player1, new NetworkTerminal());
        Card keptCard = new Forest();
        Card drawnCard = new NetworkTerminal();
        harness.setHand(player1, List.of(keptCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard, drawnCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Looting requires one mana even when both artifacts are available")
    void requiresManaPayment() {
        Permanent terminal = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
