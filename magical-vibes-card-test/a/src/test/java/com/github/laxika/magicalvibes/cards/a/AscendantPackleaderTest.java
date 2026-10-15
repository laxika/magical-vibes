package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Syncopate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AscendantPackleader.class, AirElemental.class, GrizzlyBears.class, Syncopate.class})
class AscendantPackleaderTest extends BaseCardTest {

    @Test
    void entersWithCounterWhenControllerHasPermanentWithManaValueFourOrGreater() {
        harness.addToBattlefield(player1, new AirElemental());

        Permanent packleader = castPackleader();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersWithoutCounterWhenControllerHasNoQualifyingPermanent() {
        Permanent packleader = castPackleader();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsCounterWhenControllerCastsSpellWithManaValueFourOrGreater() {
        Permanent packleader = harness.addToBattlefieldAndReturn(player1, new AscendantPackleader());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForSpellWithManaValueLessThanFour() {
        Permanent packleader = harness.addToBattlefieldAndReturn(player1, new AscendantPackleader());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithoutCounterForOpponentsQualifyingPermanent() {
        harness.addToBattlefield(player2, new AirElemental());

        Permanent packleader = castPackleader();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithoutCounterForControllersLowManaValuePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent packleader = castPackleader();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithOnlyOneCounterForMultipleQualifyingPermanents() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent packleader = castPackleader();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentsQualifyingSpell() {
        Permanent packleader = harness.addToBattlefieldAndReturn(player2, new AscendantPackleader());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsChosenXInManaValueOfCastSpell() {
        Permanent packleader = harness.addToBattlefieldAndReturn(player1, new AscendantPackleader());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Syncopate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);

        harness.castInstant(player1, 0, 3, bears.getId());
        harness.passBothPriorities();

        assertThat(packleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castPackleader() {
        harness.setHand(player1, List.of(new AscendantPackleader()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Ascendant Packleader");
    }
}
