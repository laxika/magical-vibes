package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.t.Transluminant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Darkblast.class, Transluminant.class})
class DarkblastTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -1/-1 until end of turn")
    void debuffsTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Transluminant());

        harness.setHand(player1, List.of(new Darkblast()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("May dredge Darkblast instead of drawing")
    void dredgesInsteadOfDrawing() {
        Darkblast darkblast = new Darkblast();
        List<Card> milled = List.of(new Transluminant(), new Transluminant(), new Transluminant());
        harness.setGraveyard(player1, List.of(darkblast));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(darkblast);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can decline dredge and draw normally")
    void declinesDredge() {
        Darkblast darkblast = new Darkblast();
        Card topCard = new Transluminant();
        harness.setGraveyard(player1, List.of(darkblast));
        harness.setLibrary(player1, List.of(topCard, new Transluminant(), new Transluminant()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(darkblast);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot dredge when the library has too few cards")
    void cannotDredgeWithTooFewLibraryCards() {
        Darkblast darkblast = new Darkblast();
        Card topCard = new Transluminant();
        harness.setGraveyard(player1, List.of(darkblast));
        harness.setLibrary(player1, List.of(topCard, new Transluminant()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(darkblast);
    }

    @Test
    @DisplayName("Two Darkblasts reduce a creature's toughness to zero")
    void repeatedCastsKillCreatureWithZeroToughness() {
        Transluminant creature = new Transluminant();
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        harness.setHand(player1, List.of(new Darkblast(), new Darkblast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Dredge mills only three cards and leaves the rest of the library")
    void dredgeLeavesRemainingLibraryUntouched() {
        Darkblast darkblast = new Darkblast();
        List<Card> milled = List.of(new Transluminant(), new Transluminant(), new Transluminant());
        Card remaining = new Transluminant();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(darkblast));
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), remaining));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(darkblast);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Dredge returns only the chosen copy of Darkblast")
    void dredgeReturnsChosenCopy() {
        Darkblast first = new Darkblast();
        Darkblast chosen = new Darkblast();
        List<Card> milled = List.of(new Transluminant(), new Transluminant(), new Transluminant());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(first, chosen));
        harness.setLibrary(player1, milled);

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(first, milled.get(0), milled.get(1), milled.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
