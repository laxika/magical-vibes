package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EshkiTemursRoar.class, CrawWurm.class, GrizzlyBears.class, Shock.class})
class EshkiTemursRoarTest extends BaseCardTest {

    @Test
    void lowPowerCreatureOnlyAddsACounter() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        castCreature(new GrizzlyBears(), 2);

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void fourPowerCreatureAlsoDraws() {
        Permanent eshki = addCreatureReady(player1, new EshkiTemursRoar());
        Card drawnCard = new Shock();
        harness.setLibrary(player1, List.of(drawnCard));
        castCreature(fourPowerCreature(), 2);

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
        castCreature(new CrawWurm(), 8);

        assertThat(eshki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player2, 17);
    }

    private void castCreature(Card creature, int totalMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        if (totalMana > 2) {
            harness.addMana(player1, ManaColor.COLORLESS, totalMana - 2);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Card fourPowerCreature() {
        Card creature = new Card();
        creature.setName("Four Power Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{2}");
        creature.setPower(4);
        creature.setToughness(4);
        return creature;
    }
}
