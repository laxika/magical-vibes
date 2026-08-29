package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.p.PyramidOfThePantheon;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindingConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("adds one of each counter put on an artifact or creature you control")
    void addsCountersToArtifactsAndCreatures() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 2, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(2);
    }

    @Test
    @DisplayName("adds one poison counter when you would get poison counters")
    void addsPoisonCounterToController() {
        harness.addToBattlefield(player1, new WindingConstrictor());
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
