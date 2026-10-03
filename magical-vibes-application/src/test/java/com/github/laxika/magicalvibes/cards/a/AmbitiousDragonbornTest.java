package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoneyardWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbitiousDragonborn.class, GrizzlyBears.class, HillGiant.class, Shock.class, BoneyardWurm.class})
class AmbitiousDragonbornTest extends BaseCardTest {

    @Test
    void entersWithGreatestPowerAmongControlledCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        Permanent dragonborn = castDragonborn();

        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void usesGreatestCreaturePowerFromOwnGraveyardAndIgnoresOtherCardsAndPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new HillGiant(), new Shock()));
        harness.setGraveyard(player2, List.of(new HillGiant()));

        Permanent dragonborn = castDragonborn();

        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void diesWithoutCreaturesOnBattlefieldOrInGraveyard() {
        harness.castFromHand(player1, new AmbitiousDragonborn(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ambitious Dragonborn");
        harness.assertInGraveyard(player1, "Ambitious Dragonborn");
    }

    @Test
    void ignoresGreaterPowerAmongOpponentsCreaturesAndGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setGraveyard(player2, List.of(new HillGiant()));

        Permanent dragonborn = castDragonborn();

        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void usesModifiedPowerWhenEnteringAndKeepsCountersAfterThatPowerChanges() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.castFromHand(player1, new AmbitiousDragonborn(), "{3}{G}");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        Permanent dragonborn = findPermanent(player1, "Ambitious Dragonborn");
        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void evaluatesCharacteristicDefiningPowerOfCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new BoneyardWurm(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent dragonborn = castDragonborn();

        assertThat(dragonborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private Permanent castDragonborn() {
        harness.castFromHand(player1, new AmbitiousDragonborn(), "{3}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Ambitious Dragonborn");
    }
}
