package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SilkenfistOrder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccumulatedKnowledge.class, SilkenfistOrder.class})
class AccumulatedKnowledgeTest extends BaseCardTest {

    private void castAccumulatedKnowledge() {
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);
    }

    @Test
    @DisplayName("Draws one card with no Accumulated Knowledge in graveyards")
    void drawsOneWithNoNamedCardsInGraveyards() {
        castAccumulatedKnowledge();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts Accumulated Knowledge in all graveyards")
    void countsNamedCardsInAllGraveyards() {
        gd.playerGraveyards.get(player1.getId()).add(new AccumulatedKnowledge());
        gd.playerGraveyards.get(player2.getId()).add(new AccumulatedKnowledge());

        castAccumulatedKnowledge();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Ignores cards with different names in graveyards")
    void ignoresCardsWithDifferentNames() {
        gd.playerGraveyards.get(player1.getId()).add(new SilkenfistOrder());

        castAccumulatedKnowledge();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The resolving copy does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new AccumulatedKnowledge(), new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Counts a copy that enters a graveyard after casting but before resolution")
    void countsCopyResolvedInResponse() {
        harness.setHand(player1, List.of(new AccumulatedKnowledge()));
        harness.setHand(player2, List.of(new AccumulatedKnowledge()));
        harness.setLibrary(player1, List.of(new SilkenfistOrder(), new SilkenfistOrder(),
                new SilkenfistOrder()));
        harness.setLibrary(player2, List.of(new SilkenfistOrder(), new SilkenfistOrder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Accumulated Knowledge");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Accumulated Knowledge");
    }

    @Test
    @DisplayName("Copies in hands, libraries, and exile do not increase the draw count")
    void ignoresCopiesOutsideGraveyards() {
        harness.setHand(player1, List.of(new AccumulatedKnowledge(), new AccumulatedKnowledge()));
        harness.setHand(player2, List.of(new AccumulatedKnowledge()));
        harness.setLibrary(player1, List.of(new AccumulatedKnowledge(), new SilkenfistOrder()));
        harness.setLibrary(player2, List.of(new AccumulatedKnowledge(), new SilkenfistOrder()));
        harness.setExile(player1, List.of(new AccumulatedKnowledge()));
        harness.setExile(player2, List.of(new AccumulatedKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
