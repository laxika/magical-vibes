package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EshkiTemursRoar.class, CrawWurm.class, GrizzlyBears.class, Shock.class, ThunderingGiant.class})
class EshkiTemursRoarTest extends BaseCardTest {

    @Test
    void lowPowerCreatureOnlyAddsACounter() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        castCreature(new GrizzlyBears(), "{1}{G}");

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void fourPowerCreatureAlsoDraws() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        Card drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        castCreature(new ThunderingGiant(), "{3}{R}{R}");

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void sixPowerCreatureAlsoDealsDamageEqualToEshkisPowerToEachOpponent() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        Card drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player2, 20);
        castCreature(new CrawWurm(), "{4}{G}{G}");

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player2, 17);
    }

    @Test
    void opponentsCreatureSpellDoesNotTriggerEshki() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void noncreatureSpellDoesNotTriggerEshki() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void successiveCreatureSpellsUseEshkisIncreasedPower() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        castCreature(new CrawWurm(), "{4}{G}{G}");
        harness.passBothPriorities();
        castCreature(new CrawWurm(), "{4}{G}{G}");

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 13);
    }

    @Test
    void triggerStillDrawsAndDealsLastKnownPowerDamageAfterEshkiDies() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CrawWurm(), "{4}{G}{G}");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, eshki.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eshki, Temur's Roar");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    private void castCreature(Card creature, String manaCost) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, creature, manaCost);
        harness.passBothPriorities();
    }
}
