package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhalanxLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Phalanx Leader puts a counter on each creature you control")
    void castingSpellThatTargetsLeaderPutsCountersOnControlledCreatures() {
        harness.addToBattlefield(player1, new PhalanxLeader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID leaderId = harness.getPermanentId(player1, "Phalanx Leader");
        harness.castInstant(player1, 0, leaderId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent leader = findPermanent(player1, "Phalanx Leader");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Phalanx Leader")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PhalanxLeader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent leader = findPermanent(player1, "Phalanx Leader");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Phalanx Leader does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new PhalanxLeader());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID leaderId = harness.getPermanentId(player1, "Phalanx Leader");
        harness.castInstant(player2, 0, leaderId);
        harness.passBothPriorities();

        Permanent leader = findPermanent(player1, "Phalanx Leader");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(leader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
