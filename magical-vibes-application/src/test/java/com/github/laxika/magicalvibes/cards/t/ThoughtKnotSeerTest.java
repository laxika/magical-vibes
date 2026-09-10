package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ThoughtKnotSeer.class, Forest.class, GrizzlyBears.class, Peek.class, DoomBlade.class})
class ThoughtKnotSeerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals an opponent's hand and allows choosing a nonland card to exile")
    void etbExilesChosenNonlandCard() {
        Card land = new Forest();
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(land, instant, creature)));

        castThoughtKnotSeer();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(instant);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, creature);
    }

    @Test
    @DisplayName("Leaving the battlefield makes the targeted opponent draw a card")
    void leavingBattlefieldMakesTargetOpponentDraw() {
        harness.setHand(player2, List.of(new Forest()));
        Card draw = new Peek();
        harness.setLibrary(player2, List.of(draw));
        castThoughtKnotSeer();

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Thought-Knot Seer"));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(draw);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ThoughtKnotSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void castThoughtKnotSeer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ThoughtKnotSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
