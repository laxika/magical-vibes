package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.s.SorinSolemnVisitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduHeartPiercer.class, AlpineGrizzly.class, SorinSolemnVisitor.class})
class MarduHeartPiercerTest extends BaseCardTest {

    @Test
    void etbDeals2DamageToCreatureWithRaid() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        markAttackedThisTurn();
        castMarduHeartPiercer();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpine Grizzly"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
    }

    @Test
    void etbDeals2DamageToPlayerWithRaid() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castMarduHeartPiercer();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void etbDoesNotTriggerWithoutRaid() {
        harness.setLife(player2, 20);
        castMarduHeartPiercer();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Mardu Heart-Piercer");
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        harness.setLife(player2, 20);
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castMarduHeartPiercer();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void etbCanDamageItsController() {
        harness.setLife(player1, 20);
        markAttackedThisTurn();
        castMarduHeartPiercer();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void etbCanTargetItself() {
        markAttackedThisTurn();
        castMarduHeartPiercer();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Mardu Heart-Piercer"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mardu Heart-Piercer");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void etbDealsDamageToPlaneswalker() {
        var sorin = harness.addToBattlefieldAndReturn(player2, new SorinSolemnVisitor());
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        markAttackedThisTurn();
        castMarduHeartPiercer();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Sorin, Solemn Visitor"));
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castMarduHeartPiercer() {
        harness.castFromHand(player1, new MarduHeartPiercer(), "{3}{R}");
    }
}
