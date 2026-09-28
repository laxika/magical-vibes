package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Char.class, BorosRecruit.class, ChandraNalaar.class})
class CharTest extends BaseCardTest {

    @Test
    void dealsFourToTargetPlayerAndTwoToCaster() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
    }

    @Test
    void dealsFourToTargetCreatureAndTwoToCaster() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);
        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        harness.assertInGraveyard(player2, "Boros Recruit");
    }

    @Test
    void dealsFourToTargetPlaneswalkerAndTwoToCaster() {
        harness.setLife(player1, 20);
        var target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Char()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }
}
