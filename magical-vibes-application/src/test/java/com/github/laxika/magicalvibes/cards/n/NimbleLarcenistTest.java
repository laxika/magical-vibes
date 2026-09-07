package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimbleLarcenist.class, FountainOfYouth.class, Peek.class, Divination.class,
        GrizzlyBears.class, Forest.class})
class NimbleLarcenistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB allows choosing an artifact, instant, or sorcery from an opponent's hand")
    void onlyArtifactInstantAndSorceryCardsAreChoosable() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new FountainOfYouth(), new Peek(), new Divination(), new GrizzlyBears(), new Forest())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("Chosen artifact, instant, or sorcery is exiled from the opponent's hand")
    void chosenCardIsExiled() {
        harness.setHand(player2, new ArrayList<>(List.of(new FountainOfYouth(), new Peek(), new Divination())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Fountain of Youth", "Divination");
    }

    @Test
    @DisplayName("No choice is offered when the opponent has no artifact, instant, or sorcery")
    void noValidChoices() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ETB can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new NimbleLarcenist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castAndResolveETB() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new NimbleLarcenist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
