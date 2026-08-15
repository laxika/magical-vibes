package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterpretTheSignsTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 3 is resolved before revealing the top card")
    void entersScryThree() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Mountain()));
        castInterpretTheSigns();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("Revealed card draws cards equal to its mana value")
    void drawsByRevealedManaValue() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card bottom = new Forest();
        Card rest = new Forest();
        harness.setLibrary(player1, List.of(forest, bears, bottom, rest));
        castInterpretTheSigns();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, rest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bottom);
    }

    @Test
    @DisplayName("A zero-mana-value revealed card stays on top and draws nothing")
    void zeroManaValueDrawsNothing() {
        Card mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, new Forest(), new Forest(), new Forest()));
        castInterpretTheSigns();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(mountain);
    }

    @Test
    @DisplayName("An empty library produces no draw")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        castInterpretTheSigns();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castInterpretTheSigns() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new InterpretTheSigns()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }
}
