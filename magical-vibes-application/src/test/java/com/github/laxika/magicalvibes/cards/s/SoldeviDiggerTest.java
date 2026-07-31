package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoldeviDiggerTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new SoldeviDigger());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Puts the most recently added graveyard card on the bottom of the library")
    void bottomsTopGraveyardCard() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(forest, bears));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        List<Card> deck = List.copyOf(gd.playerDecks.get(player1.getId()));
        assertThat(deck).hasSize(2);
        assertThat(deck.getLast()).isSameAs(bears);
    }

    @Test
    @DisplayName("Resolves with no effect when the graveyard is empty")
    void emptyGraveyardIsNoOp() {
        gd.playerGraveyards.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Repeated activations bottom the graveyard from the top down")
    void repeatedActivationsBottomInOrder() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(forest, bears));
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(List.copyOf(gd.playerDecks.get(player1.getId()))).containsExactly(bears, forest);
    }
}
