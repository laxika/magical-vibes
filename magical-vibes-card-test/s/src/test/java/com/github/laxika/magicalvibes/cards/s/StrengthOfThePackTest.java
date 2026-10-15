package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiltgroveStalker;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrengthOfThePack.class, GiltgroveStalker.class, TravelersAmulet.class})
class StrengthOfThePackTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on each creature you control")
    void putsCountersOnControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        Permanent ownCreatureTwo = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GiltgroveStalker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());

        harness.setHand(player1, List.of(new StrengthOfThePack()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownCreatureTwo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Adds counters to existing counters and creatures present at resolution")
    void affectsCreaturesPresentAtResolution() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        existingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new StrengthOfThePack()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        Permanent arrivingCreature = harness.enterBattlefieldAndReturn(player1, new GiltgroveStalker());
        harness.passBothPriorities();

        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(arrivingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast and resolves without any controlled creatures")
    void resolvesWithoutControlledCreatures() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GiltgroveStalker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new StrengthOfThePack()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Strength of the Pack");
        assertThat(gd.stack).isEmpty();
    }
}
