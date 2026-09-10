package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mordenkainen.class, Forest.class, GrizzlyBears.class, Island.class})
class MordenkainenTest extends BaseCardTest {

    @Test
    @DisplayName("+2 draws two cards and puts one on the bottom")
    void plusTwoDrawsAndBottomsOne() {
        Permanent mordenkainen = addReadyMordenkainen(player1, 5);
        Card kept = new GrizzlyBears();
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mordenkainen.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstDraw.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw);
    }

    @Test
    @DisplayName("-2 creates a Dog Illusion whose power and toughness track hand size")
    void minusTwoCreatesHandSizeToken() {
        addReadyMordenkainen(player1, 2);
        Card first = new Forest();
        Card second = new Island();
        Card third = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent dog = findPermanent(player1, "Dog Illusion");
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(6);

        harness.setHand(player1, List.of(first));

        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(2);
    }

    @Test
    @DisplayName("-10 exchanges hand and library, shuffles, and grants no maximum hand size")
    void minusTenExchangesHandAndLibrary() {
        Permanent mordenkainen = addReadyMordenkainen(player1, 10);
        Card handCard = new GrizzlyBears();
        Card libraryCard = new Forest();
        Card secondLibraryCard = new Island();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard, secondLibraryCard));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(mordenkainen.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard, secondLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
    }

    private Permanent addReadyMordenkainen(Player player, int loyalty) {
        Permanent perm = new Permanent(new Mordenkainen());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
