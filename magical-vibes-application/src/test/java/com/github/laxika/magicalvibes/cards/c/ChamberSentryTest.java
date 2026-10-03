package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DisdainfulStroke;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChamberSentry.class, DisdainfulStroke.class})
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

    @Test
    void repeatedManaOfOneColorAddsOnlyOneCounter() {
        harness.setHand(player1, List.of(new ChamberSentry()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castArtifact(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Chamber Sentry")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void colorlessManaAddsNoCountersAndSentryDies() {
        harness.setHand(player1, List.of(new ChamberSentry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chamber Sentry");
        harness.assertInGraveyard(player1, "Chamber Sentry");
    }

    @Test
    void removingLastCounterDoesNotStopDamageFromResolving() {
        Permanent sentry = addCreatureReady(player1, new ChamberSentry());
        sentry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.assertNotOnBattlefield(player1, "Chamber Sentry");
        harness.assertInGraveyard(player1, "Chamber Sentry");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    void cannotRemoveMoreCountersThanItHas() {
        Permanent sentry = addCreatureReady(player1, new ChamberSentry());
        sentry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sentry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sentry.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void graveyardAbilityReturnsOnlyTheActivatedCopy() {
        ChamberSentry first = new ChamberSentry();
        ChamberSentry second = new ChamberSentry();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void manaValueOnStackUsesAnnouncedXRatherThanColorsSpent() {
        ChamberSentry sentry = new ChamberSentry();
        harness.setHand(player1, List.of(sentry));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sentry.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chamber Sentry");
        harness.assertInGraveyard(player1, "Chamber Sentry");
    }
}
