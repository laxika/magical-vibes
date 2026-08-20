package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoilingDragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws two cards, then makes its controller discard a card")
    void entersDrawsTwoThenDiscards() {
        GrizzlyBears discarded = new GrizzlyBears();
        Forest firstDraw = new Forest();
        Island secondDraw = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(new RoilingDragonstorm(), discarded)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        int discardedIndex = gd.playerHands.get(player1.getId()).indexOf(discarded);
        harness.handleCardChosen(player1, discardedIndex);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Returns to its owner's hand when a Dragon you control enters")
    void returnsWhenAllyDragonEnters() {
        harness.addToBattlefield(player1, new RoilingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Roiling Dragonstorm");
        harness.assertInHand(player1, "Roiling Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when a non-Dragon creature enters")
    void doesNotReturnForNonDragon() {
        harness.addToBattlefield(player1, new RoilingDragonstorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roiling Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when an opponent's Dragon enters")
    void doesNotReturnForOpponentDragon() {
        harness.addToBattlefield(player1, new RoilingDragonstorm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ShivanDragon()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Roiling Dragonstorm");
    }
}
