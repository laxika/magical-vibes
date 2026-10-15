package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DakmorSalvage.class})
class DakmorSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Dakmor Salvage enters tapped")
    void entersTapped() {
        DakmorSalvage salvage = new DakmorSalvage();
        harness.setHand(player1, List.of(salvage));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Dakmor Salvage produces black mana")
    void producesBlackMana() {
        Permanent salvage = harness.addToBattlefieldAndReturn(player1, new DakmorSalvage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(salvage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dakmor Salvage may dredge two cards instead of drawing")
    void dredgesInsteadOfDrawing() {
        DakmorSalvage salvage = new DakmorSalvage();
        List<Card> milled = List.of(new DakmorSalvage(), new DakmorSalvage());
        harness.setGraveyard(player1, List.of(salvage));
        harness.setLibrary(player1, milled);
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(salvage);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Dakmor Salvage may be declined and draw normally")
    void declinesDredgeAndDrawsNormally() {
        DakmorSalvage salvage = new DakmorSalvage();
        DakmorSalvage topCard = new DakmorSalvage();
        DakmorSalvage nextCard = new DakmorSalvage();
        harness.setGraveyard(player1, List.of(salvage));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(salvage);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dakmor Salvage is not offered when the library has fewer than two cards")
    void cannotDredgeWithTooFewLibraryCards() {
        DakmorSalvage salvage = new DakmorSalvage();
        DakmorSalvage topCard = new DakmorSalvage();
        harness.setGraveyard(player1, List.of(salvage));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(salvage);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }
    @Test
    @DisplayName("A Dakmor Salvage milled by dredge can replace the next draw")
    void dredgesNewlyMilledCardDuringMultipleDraws() {
        DakmorSalvage original = new DakmorSalvage();
        DakmorSalvage newlyMilled = new DakmorSalvage();
        DakmorSalvage second = new DakmorSalvage();
        DakmorSalvage third = new DakmorSalvage();
        DakmorSalvage fourth = new DakmorSalvage();
        DakmorSalvage remaining = new DakmorSalvage();
        harness.setGraveyard(player1, List.of(original));
        harness.setLibrary(player1, List.of(newlyMilled, second, third, fourth, remaining));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, newlyMilled);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("An opponent's Dakmor Salvage cannot replace your draw")
    void cannotDredgeFromOpponentsGraveyard() {
        DakmorSalvage salvage = new DakmorSalvage();
        DakmorSalvage topCard = new DakmorSalvage();
        DakmorSalvage nextCard = new DakmorSalvage();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(salvage));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(salvage);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }
}
