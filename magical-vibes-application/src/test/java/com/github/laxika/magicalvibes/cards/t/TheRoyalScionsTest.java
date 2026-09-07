package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRoyalScions.class, Forest.class, GrizzlyBears.class})
class TheRoyalScionsTest extends BaseCardTest {

    @Test
    @DisplayName("The first +1 draws a card and then discards a card")
    void firstPlusOneLoots() {
        Permanent scions = addReadyScions(player1, 3);
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(scionLoyalty(scions)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("The second +1 boosts a creature with first strike and trample until end of turn")
    void secondPlusOneBoostsCreatureTemporarily() {
        addReadyScions(player1, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ultimate draws four cards and deals damage equal to the resulting hand size")
    void ultimateDrawsAndDealsHandSizeDamage() {
        Permanent scions = addReadyScions(player1, 8);
        Card firstHandCard = new GrizzlyBears();
        Card secondHandCard = new GrizzlyBears();
        harness.setHand(player1, List.of(firstHandCard, secondHandCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(scionLoyalty(scions)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private int scionLoyalty(Permanent scions) {
        return scions.getCounterCount(CounterType.LOYALTY);
    }

    private Permanent addReadyScions(Player player, int loyalty) {
        Permanent scions = new Permanent(new TheRoyalScions());
        scions.setCounterCount(CounterType.LOYALTY, loyalty);
        scions.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(scions);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return scions;
    }
}
