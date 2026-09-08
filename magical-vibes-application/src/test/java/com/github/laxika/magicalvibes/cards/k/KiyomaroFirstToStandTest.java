package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KiyomaroFirstToStand.class, GrizzlyBears.class})
class KiyomaroFirstToStandTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the controller's hand size")
    void powerAndToughnessEqualHandSize() {
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent kiyomaro = addCreatureReady(player1, new KiyomaroFirstToStand());

        assertThat(gqs.getEffectivePower(gd, kiyomaro)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kiyomaro)).isEqualTo(4);
    }

    @Test
    @DisplayName("Has vigilance with four or more cards in hand")
    void hasVigilanceAtFourCards() {
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent kiyomaro = addCreatureReady(player1, new KiyomaroFirstToStand());

        assertThat(gqs.hasKeyword(gd, kiyomaro, Keyword.VIGILANCE)).isTrue();

        gd.playerHands.get(player1.getId()).removeLast();

        assertThat(gqs.hasKeyword(gd, kiyomaro, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Gains 7 life after dealing damage with seven cards in hand")
    void gainsSevenLifeAfterDealingDamageWithSevenCards() {
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent kiyomaro = addCreatureReady(player1, new KiyomaroFirstToStand());
        harness.setLife(player1, 10);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kiyomaro)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger life gain when the hand has fewer than seven cards at damage time")
    void doesNotTriggerLifeGainWhenHandIsTooSmallAtDamageTime() {
        harness.setHand(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        Permanent kiyomaro = addCreatureReady(player1, new KiyomaroFirstToStand());
        harness.setLife(player1, 10);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(kiyomaro)));
        resolveCombat();
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Checks its damage trigger safely when it dies after dealing combat damage")
    void checksDamageTriggerAfterDyingInCombat() {
        List<Card> hand = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.setHand(player1, hand);
        Permanent kiyomaro = addCreatureReady(player1, new KiyomaroFirstToStand());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(kiyomaro),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kiyomaro.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
        assertThat(gd.stack).isEmpty();
    }
}
