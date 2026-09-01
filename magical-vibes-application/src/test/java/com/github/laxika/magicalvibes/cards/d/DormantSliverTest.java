package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DormantSliver.class, MetallicSliver.class, GrizzlyBears.class})
class DormantSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Grants defender to all Slivers, including opponents' Slivers")
    void grantsDefenderToAllSlivers() {
        addCreatureReady(player1, new DormantSliver());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("A Dormant Sliver draws a card when it enters")
    void drawsWhenItEnters() {
        harness.setHand(player1, List.of(new DormantSliver()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A Sliver entering under any player's control draws for that player")
    void drawsForSliverEnteringUnderOpponentsControl() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setHand(player2, List.of(new MetallicSliver()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Sliver entering does not draw a card")
    void doesNotDrawForNonSliver() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new MetallicSliver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
