package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
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

@CardUsed({DormantSliver.class, SinewSliver.class, HedgeTroll.class})
class DormantSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Grants defender to all Slivers, including opponents' Slivers")
    void grantsDefenderToAllSlivers() {
        addCreatureReady(player1, new DormantSliver());
        Permanent ownSliver = addCreatureReady(player1, new SinewSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SinewSliver());
        Permanent nonSliver = addCreatureReady(player1, new HedgeTroll());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Grants defender to Dormant Sliver itself")
    void grantsDefenderToSelf() {
        Permanent dormantSliver = addCreatureReady(player1, new DormantSliver());

        assertThat(gqs.hasKeyword(gd, dormantSliver, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("A Dormant Sliver draws a card when it enters")
    void drawsWhenItEnters() {
        harness.setHand(player1, List.of(new DormantSliver()));
        harness.setLibrary(player1, List.of(new HedgeTroll()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hedge Troll");
    }

    @Test
    @DisplayName("A Sliver entering under any player's control draws for that player")
    void drawsForSliverEnteringUnderOpponentsControl() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setHand(player2, List.of(new SinewSliver()));
        harness.setLibrary(player2, List.of(new HedgeTroll()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hedge Troll");
    }

    @Test
    @DisplayName("A non-Sliver entering does not draw a card")
    void doesNotDrawForNonSliver() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setHand(player1, List.of(new HedgeTroll()));
        harness.setLibrary(player1, List.of(new SinewSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
