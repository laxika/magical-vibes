package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChamberSentryTest extends BaseCardTest {

    @Test
    void entersWithOneCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new ChamberSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent sentry = findPermanent(player1, "Chamber Sentry");
        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removesCountersToDealThatMuchDamage() {
        Permanent sentry = addCreatureReady(player1, new ChamberSentry());
        sentry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void returnsItselfFromGraveyardForFiveColoredMana() {
        harness.setGraveyard(player1, List.of(new ChamberSentry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Chamber Sentry");
        harness.assertInHand(player1, "Chamber Sentry");
    }
}
