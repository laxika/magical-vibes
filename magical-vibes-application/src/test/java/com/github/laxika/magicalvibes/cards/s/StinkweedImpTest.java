package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StinkweedImp.class, CourierHawk.class, Forest.class})
class StinkweedImpTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature destroys that creature")
    void combatDamageToCreatureDestroysIt() {
        Permanent imp = addCreatureReady(player1, new StinkweedImp());
        imp.setAttacking(true);
        addCreatureReady(player2, new CourierHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Courier Hawk");
        harness.assertInGraveyard(player2, "Courier Hawk");
    }

    @Test
    @DisplayName("Combat damage to a player does not destroy a creature")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent imp = addCreatureReady(player1, new StinkweedImp());
        imp.setAttacking(true);
        addCreatureReady(player2, new CourierHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(findPermanent(player2, "Courier Hawk")).isNotNull();
    }

    @Test
    @DisplayName("May dredge Stinkweed Imp instead of drawing")
    void dredgesInsteadOfDrawing() {
        StinkweedImp imp = new StinkweedImp();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(imp));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(imp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can decline dredge and draw normally")
    void declinesDredge() {
        StinkweedImp imp = new StinkweedImp();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(imp));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest(), new Forest()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(imp);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot dredge when the library has fewer than five cards")
    void cannotDredgeWithTooFewLibraryCards() {
        StinkweedImp imp = new StinkweedImp();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(imp));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(imp);
    }

    @Test
    @DisplayName("Returns the selected Stinkweed Imp when multiple dredgers are in the graveyard")
    void choosesSelectedDredger() {
        StinkweedImp firstImp = new StinkweedImp();
        StinkweedImp selectedImp = new StinkweedImp();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(firstImp, selectedImp));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selectedImp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(
                List.of(firstImp, milled.get(0), milled.get(1), milled.get(2), milled.get(3), milled.get(4)));
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
