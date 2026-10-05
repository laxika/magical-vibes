package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Snarespinner;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PledgeOfUnity.class, Forest.class, Snarespinner.class, TotallyLost.class})
class PledgeOfUnityTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on each controlled creature and gains life for each one")
    void putsCountersOnControlledCreaturesAndGainsLife() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new Snarespinner());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PledgeOfUnity(), "{1}{G}{W}");
        harness.passBothPriorities();

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Resolves with no controlled creatures and gains no life")
    void resolvesWithoutControlledCreatures() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Snarespinner());
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new PledgeOfUnity(), "{1}{G}{W}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(ownForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Pledge of Unity");
    }

    @Test
    @DisplayName("Includes creatures that enter before resolution and adds to existing counters")
    void includesCreatureEnteringBeforeResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        existingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new PledgeOfUnity(), "{1}{G}{W}");
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new Snarespinner());

        harness.passBothPriorities();

        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(newCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Does not count a creature removed in response")
    void excludesCreatureRemovedBeforeResolution() {
        Permanent remainingCreature = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        Permanent removedCreature = harness.addToBattlefieldAndReturn(player1, new Snarespinner());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new PledgeOfUnity(), "{1}{G}{W}");
        harness.setHand(player2, List.of(new TotallyLost()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, removedCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(removedCreature);
        assertThat(remainingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(removedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }
}
